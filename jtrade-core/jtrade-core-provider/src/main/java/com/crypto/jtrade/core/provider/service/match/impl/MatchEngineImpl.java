package com.crypto.jtrade.core.provider.service.match.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import com.crypto.jtrade.core.provider.model.queue.OrderMatchEvent;
import com.crypto.jtrade.core.provider.service.match.SymbolMatcher;
import com.crypto.jtrade.core.provider.util.StatisticsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.CommandIdentity;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.util.DisruptorBuilder;
import com.crypto.jtrade.common.util.LogExceptionHandler;
import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.common.util.Utils;
import com.crypto.jtrade.core.api.model.EmptyRequest;
import com.crypto.jtrade.core.provider.model.liquidation.LiquidationCanceledOrder;
import com.crypto.jtrade.core.provider.service.match.MatchEngine;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * match engine, not the default singleton mode, but a prototype, so that every time a bean is obtained, a new bean is
 * created.
 *
 * @author 0xWill
 **/
@Service
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MatchEngineImpl implements ApplicationContextAware, MatchEngine {

    @Setter
    private ApplicationContext applicationContext;

    @Value("${jtrade.disruptor.match-buffer-size:2048}")
    private Integer matchBufferSize;

    private int engineId;

    private List<SymbolInfo> symbols;

    /**
     * KEY：symbol
     */
    private ConcurrentHashMap<String, SymbolMatcher> symbolMatchers = new ConcurrentHashMap<>();

    private Disruptor<OrderMatchEvent> matchDisruptor;

    private RingBuffer<OrderMatchEvent> matchQueue;

    /**
     * get the symbols
     */
    @Override
    public List<SymbolInfo> getSymbols() {
        return this.symbols;
    }

    /**
     * add a new symbol to the engine
     */
    @Override
    public void addSymbol(SymbolInfo symbolInfo) {
        this.symbols.add(symbolInfo);
        /**
         * init symbolMatcher
         */
        SymbolMatcher matcher = applicationContext.getBean(SymbolMatcher.class);
        matcher.init(symbolInfo);
        symbolMatchers.put(symbolInfo.getSymbol(), matcher);
    }

    /**
     * init the MatchEngine
     */
    @Override
    public void init(int engineId, List<SymbolInfo> symbols, NamedThreadFactory threadFactory) {
        this.engineId = engineId;
        this.symbols = symbols;
        /**
         * init symbolMatchers
         */
        for (SymbolInfo symbol : symbols) {
            SymbolMatcher matcher = applicationContext.getBean(SymbolMatcher.class);
            matcher.init(symbol);
            symbolMatchers.put(symbol.getSymbol(), matcher);
        }
        /**
         * init matchQueue
         */
        this.matchDisruptor = DisruptorBuilder.<OrderMatchEvent>newInstance().setRingBufferSize(matchBufferSize)
            .setEventFactory(new OrderMatchFactory()).setThreadFactory(threadFactory)
            .setProducerType(ProducerType.MULTI).setWaitStrategy(new BlockingWaitStrategy()).build();
        this.matchDisruptor.handleEventsWith(new OrderMatchHandler());
        this.matchDisruptor.setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
        this.matchQueue = this.matchDisruptor.start();
    }

    /**
     * empty command
     */
    @Override
    public void emptyCommand(EmptyRequest request) {
        publishToQueue(CommandIdentity.EMPTY_COMMAND, request);
    }

    /**
     * place order
     */
    @Override
    public void placeOrder(Order order) {
        publishToQueue(CommandIdentity.PLACE_ORDER, order);
    }

    /**
     * cancel order
     */
    @Override
    public void cancelOrder(Order order) {
        publishToQueue(CommandIdentity.CANCEL_ORDER, order);
    }

    /**
     * liquidation cancel order
     */
    @Override
    public void liquidationCancelOrder(LiquidationCanceledOrder liquidationCanceledOrder) {
        publishToQueue(CommandIdentity.LIQUIDATION_CANCEL_ORDER, liquidationCanceledOrder);
    }

    /**
     * load order
     */
    @Override
    public void loadOrder(Order order) {
        publishToQueue(CommandIdentity.LOAD_ORDER, order);
    }

    /**
     * publish depth event
     */
    @Override
    public void publishDepthEvent() {
        publishToQueue(CommandIdentity.PUBLISH_DEPTH, null);
    }

    /**
     * publish to queue
     */
    private void publishToQueue(CommandIdentity identity, Object order) {
        final EventTranslator<OrderMatchEvent> translator = (event, sequence) -> {
            event.setIdentity(identity);
            event.setOrder(order);
        };
        /**
         * FIXME: If the queue is full, publishEvent will be blocking, the system is blocked.
         */
        this.matchQueue.publishEvent(translator);
    }

    /**
     * OrderMatchEvent handler for Disruptor
     */
    private class OrderMatchHandler implements EventHandler<OrderMatchEvent> {

        @Override
        public void onEvent(final OrderMatchEvent orderMatchEvent, final long sequence, final boolean endOfBatch)
            throws Exception {
            switch (orderMatchEvent.getIdentity()) {
                case EMPTY_COMMAND:
                    emptyCommandHandler((EmptyRequest)orderMatchEvent.getOrder());
                    break;
                case PLACE_ORDER:
                    placeOrderHandler((Order)orderMatchEvent.getOrder());
                    break;
                case CANCEL_ORDER:
                    cancelOrderHandler((Order)orderMatchEvent.getOrder());
                    break;
                case LIQUIDATION_CANCEL_ORDER:
                    liquidationCancelOrderHandler((LiquidationCanceledOrder)orderMatchEvent.getOrder());
                    break;
                case LOAD_ORDER:
                    loadOrderHandler((Order)orderMatchEvent.getOrder());
                    break;
                case PUBLISH_DEPTH:
                    publishDepthEventHandler();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * empty command handler
     */
    private void emptyCommandHandler(EmptyRequest request) {
        CountDownLatch latch = request.getLatch();
        if (latch != null) {
            latch.countDown();
        }
    }

    /**
     * place order handler
     */
    private void placeOrderHandler(Order order) {
        long startTime = Utils.currentMicroTime();
        SymbolMatcher matcher = getSymbolMatcher(order.getSymbol());
        matcher.placeOrder(order);
        // statistics execute time
        if (StatisticsHelper.enabled()) {
            log.info("place order in match queue execute_time: {}us", Utils.currentMicroTime() - startTime);
        }
    }

    /**
     * cancel order handler
     */
    private void cancelOrderHandler(Order order) {
        long startTime = Utils.currentMicroTime();
        SymbolMatcher matcher = getSymbolMatcher(order.getSymbol());
        matcher.cancelOrder(order);
        // statistics execute time
        if (StatisticsHelper.enabled()) {
            log.info("cancel order in match queue execute_time: {}us", Utils.currentMicroTime() - startTime);
        }
    }

    /**
     * liquidation cancel order handler
     */
    private void liquidationCancelOrderHandler(LiquidationCanceledOrder liquidationCanceledOrder) {
        Order order = liquidationCanceledOrder.getOrder();
        SymbolMatcher matcher = getSymbolMatcher(order.getSymbol());
        matcher.cancelOrder(order);

        CountDownLatch latch = liquidationCanceledOrder.getLatch();
        if (latch != null) {
            latch.countDown();
        }
    }

    /**
     * load order handler
     */
    private void loadOrderHandler(Order order) {
        SymbolMatcher matcher = getSymbolMatcher(order.getSymbol());
        matcher.placeOrder(order);
    }

    /**
     * publish depth event
     */
    private void publishDepthEventHandler() {
        for (String symbol : symbolMatchers.keySet()) {
            SymbolMatcher matcher = getSymbolMatcher(symbol);
            matcher.publishDepthEvent();
        }
    }

    /**
     * get the matcher of the symbol
     */
    private SymbolMatcher getSymbolMatcher(String symbol) {
        SymbolMatcher matcher = symbolMatchers.get(symbol);
        if (matcher == null) {
            throw new TradeException(TradeError.SYMBOL_MATCHER_NOT_EXIST);
        } else {
            return matcher;
        }
    }

    /**
     * OrderMatchEvent factory for Disruptor
     */
    private static class OrderMatchFactory implements EventFactory<OrderMatchEvent> {

        @Override
        public OrderMatchEvent newInstance() {
            return new OrderMatchEvent();
        }
    }

}
