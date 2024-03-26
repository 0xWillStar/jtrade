package com.crypto.jtrade.core.provider.service.oto.impl;

import java.math.BigDecimal;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.OTOOrderType;
import com.crypto.jtrade.common.constants.OrderStatus;
import com.crypto.jtrade.common.model.ComplexEntity;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.util.DisruptorBuilder;
import com.crypto.jtrade.common.util.LogExceptionHandler;
import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.core.api.model.CancelOrderRequest;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.oto.OTOService;
import com.crypto.jtrade.core.provider.service.trade.TradeCommand;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import lombok.extern.slf4j.Slf4j;

/**
 * OTO service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class OTOServiceImpl implements OTOService {

    @Value("${jtrade.disruptor.oto-buffer-size:2048}")
    private Integer otoBufferSize;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private TradeCommand tradeCommand;

    private Disruptor<ComplexEntity> otoDisruptor;

    private RingBuffer<ComplexEntity> otoQueue;

    @PostConstruct
    public void init() {
        initOTOQueue();
    }

    @Override
    public void receiveOTOEvent(ComplexEntity complexEntity) {
        publishToQueue(complexEntity);
    }

    /**
     * publish to queue
     */
    private void publishToQueue(ComplexEntity complexEntity) {
        final EventTranslator<ComplexEntity> translator = (event, sequence) -> {
            event.setOrder(complexEntity.getOrder());
        };
        /**
         * FIXME: If the queue is full, publishEvent will be blocking, the system is blocked.
         */
        this.otoQueue.publishEvent(translator);
    }

    /**
     * OTO handler for Disruptor
     */
    private class OTOHandler implements EventHandler<ComplexEntity> {

        @Override
        public void onEvent(final ComplexEntity complexEntity, final long sequence, final boolean endOfBatch)
            throws Exception {
            Order order = complexEntity.getOrder();
            if (order == null || order.getOtoOrderType() == OTOOrderType.NONE) {
                return;
            }

            if (order.getOtoOrderType() == OTOOrderType.PRIMARY) {
                if (order.getStatus() == OrderStatus.CANCELED) {
                    /**
                     * If no executed primary order was canceled, all secondary orders will be canceled.
                     */
                    if (order.getExecutedQty().compareTo(BigDecimal.ZERO) == 0) {
                        if (order.getSubOrderId1() != null) {
                            cancelSecondaryOrder(order.getSubOrderId1(), order);
                        }
                        if (order.getSubOrderId2() != null) {
                            cancelSecondaryOrder(order.getSubOrderId2(), order);
                        }
                    } else {
                        /**
                         * If partially executed primary order was canceled, all secondary orders will be placed.
                         */
                        if (order.getSubOrderId1() != null) {
                            triggerSecondaryOrder(order.getSubOrderId1(), order);
                        }
                        if (order.getSubOrderId2() != null) {
                            triggerSecondaryOrder(order.getSubOrderId2(), order);
                        }
                    }
                } else if (order.getStatus() == OrderStatus.FILLED) {
                    /**
                     * If primary order is filled fully, all secondary orders will be placed.
                     */
                    if (order.getSubOrderId1() != null) {
                        triggerSecondaryOrder(order.getSubOrderId1(), order);
                    }
                    if (order.getSubOrderId2() != null) {
                        triggerSecondaryOrder(order.getSubOrderId2(), order);
                    }
                }
            } else if (order.getOtoOrderType() == OTOOrderType.SECONDARY) {
                /**
                 * If one secondary order is filled fully, the other secondary order will be canceled.
                 */
                if (order.getStatus() == OrderStatus.FILLED && order.getSubOrderId1() != null) {
                    cancelSecondaryOrder(order.getSubOrderId1(), order);
                }
            }
        }
    }

    /**
     * cancel secondary order
     */
    private void cancelSecondaryOrder(Long orderId, Order order) {
        CancelOrderRequest request = new CancelOrderRequest();
        request.setClientId(order.getClientId());
        request.setSymbol(order.getSymbol());
        request.setOrderId(orderId);
        tradeCommand.cancelOrder(request);
    }

    /**
     * trigger secondary order
     */
    private void triggerSecondaryOrder(Long orderId, Order order) {
        Order secondaryOrder = localCache.getClientEntity(order.getClientId()).getOrderByOrderId(orderId);
        if (secondaryOrder != null) {
            tradeCommand.triggerSecondaryOrder(secondaryOrder);
        }
    }

    /**
     * init OTO queue
     */
    private void initOTOQueue() {
        this.otoDisruptor = DisruptorBuilder.<ComplexEntity>newInstance().setRingBufferSize(otoBufferSize)
            .setEventFactory(new OTOServiceImpl.OTOFactory())
            .setThreadFactory(new NamedThreadFactory("jtrade-oto-disruptor-", true)).setProducerType(ProducerType.MULTI)
            .setWaitStrategy(new BlockingWaitStrategy()).build();
        this.otoDisruptor.handleEventsWith(new OTOServiceImpl.OTOHandler());
        this.otoDisruptor.setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
        this.otoQueue = this.otoDisruptor.start();
    }

    /**
     * OTO factory for Disruptor
     */
    private static class OTOFactory implements EventFactory<ComplexEntity> {

        @Override
        public ComplexEntity newInstance() {
            return new ComplexEntity();
        }
    }
}
