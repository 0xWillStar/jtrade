package com.crypto.jtrade.core.provider.service.trade.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.CommandIdentity;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.util.DisruptorBuilder;
import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.core.api.model.*;
import com.crypto.jtrade.core.provider.model.queue.CommandEvent;
import com.crypto.jtrade.core.provider.service.cache.RedisService;
import com.crypto.jtrade.core.provider.service.trade.TradeCommand;
import com.crypto.jtrade.core.provider.service.trade.TradeLog;
import com.crypto.jtrade.core.provider.service.trade.TradeService;
import com.crypto.jtrade.core.provider.util.SequenceHelper;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import lombok.extern.slf4j.Slf4j;

/**
 * trade command service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class TradeCommandImpl implements TradeCommand {

    @Value("${jtrade.disruptor.trade-command-buffer-size:8192}")
    private Integer tradeCommandBufferSize;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TradeLog tradeLog;

    @Autowired
    private TradeService tradeService;

    private Disruptor<CommandEvent> tradeCommandDisruptor;

    private RingBuffer<CommandEvent> tradeCommandQueue;

    @PostConstruct
    public void init() {
        initTradeCommandQueue();
    }

    /**
     * empty command
     */
    @Override
    public BaseResponse emptyCommand(EmptyRequest request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.EMPTY_COMMAND, request, future);
        return getResponse(future);
    }

    /**
     * set system parameter
     */
    @Override
    public BaseResponse setSystemParameter(SystemParameterRequest request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.SET_SYSTEM_PARAMETER, request, future);
        return getResponse(future);
    }

    /**
     * set symbol info
     */
    @Override
    public BaseResponse setSymbolInfo(SymbolInfoRequest request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.SET_SYMBOL_INFO, request, future);
        return getResponse(future);
    }

    /**
     * set symbol indicator
     */
    @Override
    public BaseResponse setSymbolIndicator(SymbolIndicatorRequest request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.SET_SYMBOL_INDICATOR, request, future);
        return getResponse(future);
    }

    /**
     * set asset info
     */
    @Override
    public BaseResponse setAssetInfo(AssetInfoRequest request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.SET_ASSET_INFO, request, future);
        return getResponse(future);
    }

    /**
     * set funding rate
     */
    @Override
    public BaseResponse setFundingRate(List<FundingRateRequest> request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.SET_FUNDING_RATE, request, future);
        return getResponse(future);
    }

    /**
     * set mark price
     */
    @Override
    public BaseResponse setMarkPrice(List<MarkPriceRequest> request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.SET_MARK_PRICE, request, future);
        return getResponse(future);
    }

    /**
     * set fee rate by client
     */
    @Override
    public BaseResponse setClientFeeRate(List<ClientFeeRateRequest> request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.SET_CLIENT_FEE_RATE, request, future);
        return getResponse(future);
    }

    /**
     * set trade authority by client
     */
    @Override
    public BaseResponse setClientTradeAuthority(List<TradeAuthorityRequest> request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.SET_CLIENT_TRADE_AUTHORITY, request, future);
        return getResponse(future);
    }

    /**
     * set client setting
     */
    @Override
    public BaseResponse setClientSetting(ClientSettingRequest request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.SET_CLIENT_SETTING, request, future);
        return getResponse(future);
    }

    /**
     * deposit
     */
    @Override
    public BaseResponse deposit(DepositRequest request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.DEPOSIT, request, future);
        return getResponse(future);
    }

    /**
     * withdraw
     */
    @Override
    public BaseResponse withdraw(WithdrawRequest request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.WITHDRAW, request, future);
        return getResponse(future);
    }

    /**
     * place order
     */
    @Override
    public BaseResponse<PlaceOrderResponse> placeOrder(PlaceOrderRequest request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.PLACE_ORDER, request, future);
        return getResponse(future);
    }

    /**
     * place OTO order
     */
    @Override
    public BaseResponse<PlaceOrderResponse> placeOTOOrder(List<PlaceOrderRequest> request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.PLACE_OTO_ORDER, request, future);
        return getResponse(future);
    }

    /**
     * place order when stop order triggered
     */
    @Override
    public BaseResponse stopTriggeredPlaceOrder(Order request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.STOP_TRIGGERED_PLACE_ORDER, request, future);
        return getResponse(future);
    }

    /**
     * cancel order
     */
    @Override
    public BaseResponse cancelOrder(CancelOrderRequest request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.CANCEL_ORDER, request, future);
        return getResponse(future);
    }

    @Override
    public BaseResponse stopRejectedCancelOrder(Order request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.STOP_REJECTED_CANCEL_ORDER, request, future);
        return getResponse(future);
    }

    /**
     * cancel order when liquidation
     */
    @Override
    public BaseResponse liquidationCancelOrder(LiquidationCancelOrderRequest request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.LIQUIDATION_CANCEL_ORDER, request, future);
        return getResponse(future);
    }

    @Override
    public BaseResponse triggerSecondaryOrder(Order request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.TRIGGER_SECONDARY_ORDER, request, future);
        return getResponse(future);
    }

    /**
     * OTC trade
     */
    @Override
    public BaseResponse otcTrade(OTCRequest request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.OTC_TRADE, request, future);
        return getResponse(future);
    }

    @Override
    public BaseResponse adjustPositionMargin(AdjustPositionMarginRequest request) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.ADJUST_POSITION_MARGIN, request, future);
        return getResponse(future);
    }

    @Override
    public BaseResponse deductCollateralAssets(String clientId) {
        CompletableFuture<BaseResponse> future = new CompletableFuture<>();
        publishToQueue(CommandIdentity.DEDUCT_COLLATERAL, clientId, future);
        return getResponse(future);
    }

    /**
     * publish to queue
     */
    private void publishToQueue(CommandIdentity identity, Object request, CompletableFuture<BaseResponse> future) {
        final EventTranslator<CommandEvent> translator = (event, sequence) -> {
            event.setIdentity(identity);
            event.setRequest(request);
            event.setFuture(future);
        };
        if (!this.tradeCommandQueue.tryPublishEvent(translator)) {
            log.error("System is busy, has too many requests, queue is full and bufferSize={}",
                this.tradeCommandQueue.getBufferSize());
            throw new TradeException(TradeError.REQUEST_TOO_MANY);
        }
    }

    /**
     * JCommand handler for Disruptor
     */
    private class JCommandHandler implements EventHandler<CommandEvent> {

        @Override
        public void onEvent(final CommandEvent commandEvent, final long sequence, final boolean endOfBatch)
            throws Exception {
            final Long requestId = SequenceHelper.incrementAndGetRequestId();
            commandEvent.setRequestId(requestId);

            /**
             * save trading command log to redis
             */
            redisService.saveCommandLog(commandEvent);
            /**
             * publish command to trade log
             */
            tradeLog.publishLog(commandEvent);

            /**
             * publish command to trade service
             */
            tradeService.publishCommand(commandEvent);
        }
    }

    /**
     * CommandEvent factory for Disruptor
     */
    private static class CommandFactory implements EventFactory<CommandEvent> {

        @Override
        public CommandEvent newInstance() {
            return new CommandEvent();
        }
    }

    /**
     * init the tradeCommandQueue
     */
    private void initTradeCommandQueue() {
        this.tradeCommandDisruptor = DisruptorBuilder.<CommandEvent>newInstance()
            .setRingBufferSize(tradeCommandBufferSize).setEventFactory(new CommandFactory())
            .setThreadFactory(new NamedThreadFactory("jtrade-trade-command-disruptor-", true))
            .setProducerType(ProducerType.MULTI).setWaitStrategy(new BlockingWaitStrategy()).build();
        this.tradeCommandDisruptor.handleEventsWith(new JCommandHandler());
        this.tradeCommandDisruptor
            .setDefaultExceptionHandler(new CommandLogExceptionHandler<CommandEvent>(getClass().getSimpleName()));
        this.tradeCommandQueue = this.tradeCommandDisruptor.start();
    }

    /**
     * get response
     */
    BaseResponse getResponse(CompletableFuture<BaseResponse> future) {
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException t) {
            throw new TradeException(TradeError.REQUEST_PROCESS_TIMEOUT);
        } catch (Exception e) {
            log.error("get response from future", e);
            throw new TradeException(TradeError.INTERNAL);
        }
    }

}
