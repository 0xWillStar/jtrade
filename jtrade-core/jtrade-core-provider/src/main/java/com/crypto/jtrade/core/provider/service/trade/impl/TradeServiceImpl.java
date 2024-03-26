package com.crypto.jtrade.core.provider.service.trade.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.crypto.jtrade.common.constants.CommandIdentity;
import com.crypto.jtrade.common.constants.OTOOrderType;
import com.crypto.jtrade.common.constants.OrderType;
import com.crypto.jtrade.common.constants.SymbolStatus;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.*;
import com.crypto.jtrade.common.util.DisruptorBuilder;
import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.common.util.ResponseHelper;
import com.crypto.jtrade.common.util.Utils;
import com.crypto.jtrade.core.api.model.*;
import com.crypto.jtrade.core.provider.model.convert.BeanMapping;
import com.crypto.jtrade.core.provider.model.landing.*;
import com.crypto.jtrade.core.provider.model.liquidation.LiquidationCanceledOrder;
import com.crypto.jtrade.core.provider.model.queue.CommandEvent;
import com.crypto.jtrade.core.provider.model.session.OrderSession;
import com.crypto.jtrade.core.provider.service.cache.ClientEntity;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.landing.MySqlLanding;
import com.crypto.jtrade.core.provider.service.landing.RedisLanding;
import com.crypto.jtrade.core.provider.service.match.MatchEngine;
import com.crypto.jtrade.core.provider.service.match.MatchEngineManager;
import com.crypto.jtrade.core.provider.service.rule.TradeRule;
import com.crypto.jtrade.core.provider.service.rule.TradeRuleManager;
import com.crypto.jtrade.core.provider.service.trade.TradeService;
import com.crypto.jtrade.core.provider.util.ClientLockHelper;
import com.crypto.jtrade.core.provider.util.OrderSessionHelper;
import com.crypto.jtrade.core.provider.util.SequenceHelper;
import com.crypto.jtrade.core.provider.util.StatisticsHelper;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import lombok.extern.slf4j.Slf4j;

/**
 * Trade service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class TradeServiceImpl implements TradeService {

    @Value("${jtrade.disruptor.trade-buffer-size:8192}")
    private Integer tradeBufferSize;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private RedisLanding redisLanding;

    @Autowired
    private MySqlLanding mySqlLanding;

    @Autowired
    private BeanMapping beanMapping;

    @Autowired
    private MatchEngineManager matchEngineManager;

    @Autowired
    private TradeRuleManager tradeRuleManager;

    private Disruptor<CommandEvent> tradeDisruptor;

    private RingBuffer<CommandEvent> tradeQueue;

    @PostConstruct
    public void init() {
        initTradeQueue();
    }

    @Override
    public void publishCommand(CommandEvent commandEvent) {
        final EventTranslator<CommandEvent> translator = (event, sequence) -> {
            event.setRequestId(commandEvent.getRequestId());
            event.setIdentity(commandEvent.getIdentity());
            event.setRequest(commandEvent.getRequest());
            event.setFuture(commandEvent.getFuture());
        };
        if (!this.tradeQueue.tryPublishEvent(translator)) {
            log.error("System is busy, has too many requests, queue is full and bufferSize={}",
                this.tradeQueue.getBufferSize());
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
            switch (commandEvent.getIdentity()) {
                case EMPTY_COMMAND:
                    emptyCommandHandler(commandEvent);
                    break;
                case SET_SYSTEM_PARAMETER:
                    setSystemParameterHandler(commandEvent);
                    break;
                case SET_SYMBOL_INFO:
                    setSymbolInfoHandler(commandEvent);
                    break;
                case SET_SYMBOL_INDICATOR:
                    setSymbolIndicatorHandler(commandEvent);
                    break;
                case SET_ASSET_INFO:
                    setAssetInfoHandler(commandEvent);
                    break;
                case SET_FUNDING_RATE:
                    setFundingRateHandler(commandEvent);
                    break;
                case SET_MARK_PRICE:
                    setMarkPriceHandler(commandEvent);
                    break;
                case SET_CLIENT_FEE_RATE:
                    setClientFeeRateHandler(commandEvent);
                    break;
                case SET_CLIENT_TRADE_AUTHORITY:
                    setClientTradeAuthorityHandler(commandEvent);
                    break;
                case SET_CLIENT_SETTING:
                    setClientSettingHandler(commandEvent);
                    break;
                case DEPOSIT:
                    depositHandler(commandEvent);
                    break;
                case WITHDRAW:
                    withdrawHandler(commandEvent);
                    break;
                case PLACE_ORDER:
                    placeOrderHandler(commandEvent);
                    break;
                case PLACE_OTO_ORDER:
                    placeOTOOrderHandler(commandEvent);
                    break;
                case STOP_TRIGGERED_PLACE_ORDER:
                    stopTriggeredPlaceOrderHandler(commandEvent);
                    break;
                case CANCEL_ORDER:
                    cancelOrderHandler(commandEvent);
                    break;
                case STOP_REJECTED_CANCEL_ORDER:
                    stopRejectedCancelOrderHandler(commandEvent);
                    break;
                case LIQUIDATION_CANCEL_ORDER:
                    liquidationCancelOrderHandler(commandEvent);
                    break;
                case TRIGGER_SECONDARY_ORDER:
                    triggerSecondaryOrderHandler(commandEvent);
                    break;
                case OTC_TRADE:
                    otcTradeHandler(commandEvent);
                    break;
                case ADJUST_POSITION_MARGIN:
                    adjustPositionMarginHandler(commandEvent);
                    break;
                case DEDUCT_COLLATERAL:
                    deductCollateralHandler(commandEvent);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * empty commandEvent handler
     */
    private void emptyCommandHandler(CommandEvent<EmptyRequest> commandEvent) {
        EmptyRequest request = commandEvent.getRequest();
        MatchEngine matchEngine = matchEngineManager.getMatchEngine(request.getSymbol());
        matchEngine.emptyCommand(request);
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * set system parameter handler
     */
    private void setSystemParameterHandler(CommandEvent<SystemParameterRequest> commandEvent) {
        final SystemParameterRequest request = commandEvent.getRequest();
        /**
         * update local cache first
         */
        switch (request.getParameter()) {
            case LAST_REQUEST_ID:
                SequenceHelper.setRequestId(Long.valueOf(request.getValue()));
                break;
            case LAST_ORDER_ID:
                SequenceHelper.setOrderId(Long.valueOf(request.getValue()));
                break;
            case LAST_TRADE_ID:
                SequenceHelper.setTradeId(Long.valueOf(request.getValue()));
                break;
            case STATISTICS_ENABLED:
                StatisticsHelper.setStatisticsEnabled(Boolean.parseBoolean(request.getValue()));
                break;
            default:
                localCache.setSystemParameter(request.getParameter(), request.getValue());
                break;
        }

        /**
         * write to redis
         */
        SystemParameterLanding landing =
            SystemParameterLanding.builder().requestId(commandEvent.getRequestId()).parameter(request).build();
        redisLanding.setSystemParameter(landing);
        /**
         * write to mysql
         */
        mySqlLanding.setSystemParameter(landing);

        /**
         * release future, the request can be answered.
         */
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * set symbol info handler
     */
    private void setSymbolInfoHandler(CommandEvent<SymbolInfoRequest> commandEvent) {
        SymbolInfo symbolInfo = commandEvent.getRequest();
        ReentrantLock lock = localCache.getSymbolWriteLock();
        lock.lock();
        try {
            checkSymbolStatus(symbolInfo);
            // update local cache
            localCache.setSymbolInfo(symbolInfo);
            // write to redis
            SymbolInfoLanding landing =
                SymbolInfoLanding.builder().requestId(commandEvent.getRequestId()).symbolInfo(symbolInfo).build();
            redisLanding.setSymbolInfo(landing);
            // write to mysql
            mySqlLanding.setSymbolInfo(landing);
        } finally {
            lock.unlock();
        }
        // release future, the request can be answered.
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * set symbol indicator handler
     */
    private void setSymbolIndicatorHandler(CommandEvent<SymbolIndicatorRequest> commandEvent) {
        SymbolIndicatorRequest request = commandEvent.getRequest();
        SymbolIndicator indicator = localCache.getSymbolIndicator(request.getSymbol());
        switch (request.getIndicatorType()) {
            case INDEX_PRICE:
                indicator.setIndexPrice(new BigDecimal(request.getValue()));
                break;
            case MARK_PRICE:
                indicator.setMarkPrice(new BigDecimal(request.getValue()));
                break;
            case FUNDING_RATE:
                indicator.setFundingRate(new BigDecimal(request.getValue()));
                break;
            default:
                break;
        }
        /**
         * TODO: The last requestId is not save to redis.
         */
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * set asset info handler
     */
    private void setAssetInfoHandler(CommandEvent<AssetInfoRequest> commandEvent) {
        AssetInfo assetInfo = commandEvent.getRequest();
        // update local cache
        localCache.setAssetInfo(assetInfo);
        // write to redis
        AssetInfoLanding landing =
            AssetInfoLanding.builder().requestId(commandEvent.getRequestId()).assetInfo(assetInfo).build();
        redisLanding.setAssetInfo(landing);
        // write to mysql
        mySqlLanding.setAssetInfo(landing);

        // release future, the request can be answered.
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * set funding rate
     */
    private void setFundingRateHandler(CommandEvent<List<FundingRateRequest>> commandEvent) {
        long startTime = Utils.currentMicroTime();
        List<FundingRateRequest> request = commandEvent.getRequest();
        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.SET_FUNDING_RATE);
        for (TradeRule rule : tradeRules) {
            rule.setFundingRate(commandEvent.getRequestId(), request);
        }
        commandEvent.getFuture().complete(ResponseHelper.success());
        // statistics execute time
        if (StatisticsHelper.enabled()) {
            log.info("set funding rate in commandEvent queue execute_time: {}us", Utils.currentMicroTime() - startTime);
        }
    }

    /**
     * set mark price
     */
    private void setMarkPriceHandler(CommandEvent<List<MarkPriceRequest>> commandEvent) {
        List<MarkPriceRequest> requestList = commandEvent.getRequest();
        for (MarkPriceRequest request : requestList) {
            localCache.getSymbolIndicator(request.getSymbol()).setMarkPrice(request.getMarkPrice());
        }
        /**
         * TODO: The last requestId is not save to redis.
         */
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * set fee rate by client
     */
    private void setClientFeeRateHandler(CommandEvent<List<ClientFeeRateRequest>> commandEvent) {
        List<ClientFeeRateRequest> requestList = commandEvent.getRequest();
        if (!CollectionUtils.isEmpty(requestList)) {
            List<FeeRate> feeRateList = new ArrayList<>(requestList.size());
            for (ClientFeeRateRequest request : requestList) {
                /**
                 * update local cache, no need to lock here.
                 */
                ClientEntity clientEntity = localCache.getClientEntity(request.getClientId());
                clientEntity.setFeeRate(request);
                feeRateList.add(request);
            }
            ClientFeeRateLanding landing = new ClientFeeRateLanding(commandEvent.getRequestId(), feeRateList);
            redisLanding.setClientFeeRate(landing);
            mySqlLanding.setClientFeeRate(landing);
        }
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * set trade authority by client
     */
    private void setClientTradeAuthorityHandler(CommandEvent<List<TradeAuthorityRequest>> commandEvent) {
        List<TradeAuthorityRequest> requestList = commandEvent.getRequest();
        if (!CollectionUtils.isEmpty(requestList)) {
            List<TradeAuthority> tradeAuthorityList = new ArrayList<>(requestList.size());
            for (TradeAuthorityRequest request : requestList) {
                /**
                 * update local cache, no need to lock here.
                 */
                ClientEntity clientEntity = localCache.getClientEntity(request.getClientId());
                clientEntity.setTradeAuthority(request.getTradeAuthority());
                tradeAuthorityList.add(request);
            }
            ClientTradeAuthorityLanding landing =
                new ClientTradeAuthorityLanding(commandEvent.getRequestId(), tradeAuthorityList);
            redisLanding.setClientTradeAuthority(landing);
            mySqlLanding.setClientTradeAuthority(landing);
        }
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * set client setting handler
     */
    private void setClientSettingHandler(CommandEvent<ClientSettingRequest> commandEvent) {
        ClientSettingRequest request = commandEvent.getRequest();
        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.SET_CLIENT_SETTING);
        Lock lock = ClientLockHelper.getLock(request.getClientId());
        lock.lock();
        try {
            for (TradeRule rule : tradeRules) {
                rule.setClientSetting(commandEvent.getRequestId(), request);
            }
        } finally {
            lock.unlock();
        }
        /**
         * release future, the request can be answered.
         */
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * deposit handler
     */
    private void depositHandler(CommandEvent<DepositRequest> commandEvent) {
        DepositRequest request = commandEvent.getRequest();
        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.DEPOSIT);
        Lock lock = ClientLockHelper.getLock(request.getClientId());
        lock.lock();
        try {
            for (TradeRule rule : tradeRules) {
                rule.deposit(commandEvent.getRequestId(), request);
            }
        } finally {
            lock.unlock();
        }
        /**
         * release future, the request can be answered.
         */
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * withdraw handler
     */
    private void withdrawHandler(CommandEvent<WithdrawRequest> commandEvent) {
        WithdrawRequest request = commandEvent.getRequest();
        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.WITHDRAW);
        Lock lock = ClientLockHelper.getLock(request.getClientId());
        lock.lock();
        try {
            for (TradeRule rule : tradeRules) {
                rule.withdraw(commandEvent.getRequestId(), request);
            }
        } finally {
            lock.unlock();
        }
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * place order handler
     */
    private void placeOrderHandler(CommandEvent<PlaceOrderRequest> commandEvent) {
        long startTime = Utils.currentMicroTime();
        PlaceOrderRequest request = commandEvent.getRequest();
        // for non-OTO orders, ignore this field.
        request.setOtoOrderType(OTOOrderType.NONE);

        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.PLACE_ORDER);
        Order order;
        Lock lock = ClientLockHelper.getLock(request.getClientId());
        lock.lock();
        try {
            // get and init session
            OrderSession session = OrderSessionHelper.get();
            session.init(request.getClientId(), request.getSymbol(), localCache, commandEvent.getRequestId());
            // execute trade rule
            for (TradeRule rule : tradeRules) {
                rule.placeOrder(request, session);
            }
            // set the new order
            order = session.getOrder();
        } finally {
            lock.unlock();
        }

        if (order.getType() == OrderType.LIMIT || order.getType() == OrderType.MARKET) {
            // send to match engine
            MatchEngine matchEngine = matchEngineManager.getMatchEngine(request.getSymbol());
            matchEngine.placeOrder(order);
        }
        // release future, the request can be answered.
        PlaceOrderResponse response = new PlaceOrderResponse(order.getClientOrderId(), order.getOrderId());
        commandEvent.getFuture().complete(ResponseHelper.success(response));
        // statistics execute time
        if (StatisticsHelper.enabled()) {
            log.info("place order in commandEvent queue execute_time: {}us", Utils.currentMicroTime() - startTime);
        }
    }

    /**
     * place OTO order
     */
    private void placeOTOOrderHandler(CommandEvent<List<PlaceOrderRequest>> commandEvent) {
        List<PlaceOrderRequest> requestList = commandEvent.getRequest();
        Map<OTOOrderType, List<PlaceOrderRequest>> requestMap = checkOTOOrders(requestList);
        PlaceOrderRequest primaryRequest = requestMap.get(OTOOrderType.PRIMARY).get(0);
        List<PlaceOrderRequest> secondaryRequests = requestMap.get(OTOOrderType.SECONDARY);

        Long subOrderId1 = SequenceHelper.incrementAndGetOrderId();
        Long subOrderId2 = secondaryRequests.size() == 2 ? SequenceHelper.incrementAndGetOrderId() : null;
        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.PLACE_ORDER);
        Order primaryOrder;
        Lock lock = ClientLockHelper.getLock(primaryRequest.getClientId());
        lock.lock();
        try {
            // get and init session
            OrderSession session = OrderSessionHelper.get();
            session.init(primaryRequest.getClientId(), primaryRequest.getSymbol(), localCache,
                commandEvent.getRequestId());
            session.setSubOrderId1(subOrderId1);
            session.setSubOrderId2(subOrderId2);
            /**
             * primary order
             */
            for (TradeRule rule : tradeRules) {
                rule.placeOrder(primaryRequest, session);
            }
            primaryOrder = session.getOrder();
            /**
             * secondary order
             */
            int i = 0;
            for (PlaceOrderRequest orderRequest : secondaryRequests) {
                session.setOrder(null);
                if (++i == 2) {
                    session.setSubOrderId1(subOrderId2);
                    session.setSubOrderId2(subOrderId1);
                }
                for (TradeRule rule : tradeRules) {
                    rule.placeOrder(orderRequest, session);
                }
            }
        } finally {
            lock.unlock();
        }

        if (primaryOrder.getType() == OrderType.LIMIT || primaryOrder.getType() == OrderType.MARKET) {
            // send to match engine
            MatchEngine matchEngine = matchEngineManager.getMatchEngine(primaryOrder.getSymbol());
            matchEngine.placeOrder(primaryOrder);
        }
        // release future, the request can be answered.
        PlaceOrderResponse response =
            new PlaceOrderResponse(primaryOrder.getClientOrderId(), primaryOrder.getOrderId());
        commandEvent.getFuture().complete(ResponseHelper.success(response));
    }

    /**
     * place order handler when stop order triggered
     */
    private void stopTriggeredPlaceOrderHandler(CommandEvent<Order> commandEvent) {
        Order triggeredOrder = commandEvent.getRequest();
        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.PLACE_ORDER);
        Order order;
        Lock lock = ClientLockHelper.getLock(triggeredOrder.getClientId());
        lock.lock();
        try {
            // get and init session
            OrderSession session = OrderSessionHelper.get();
            session.init(triggeredOrder.getClientId(), triggeredOrder.getSymbol(), localCache,
                commandEvent.getRequestId());
            session.setStopTriggered(true);
            session.setOrder(triggeredOrder);
            if (triggeredOrder.getOtoOrderType() != OTOOrderType.NONE) {
                session.setSubOrderId1(triggeredOrder.getSubOrderId1());
                session.setSubOrderId2(triggeredOrder.getSubOrderId2());
            }

            PlaceOrderRequest request = beanMapping.convert(triggeredOrder);
            // execute trade rule
            for (TradeRule rule : tradeRules) {
                rule.placeOrder(request, session);
            }
            // set the new order
            order = session.getOrder();
        } finally {
            lock.unlock();
        }

        // send to match engine
        MatchEngine matchEngine = matchEngineManager.getMatchEngine(triggeredOrder.getSymbol());
        matchEngine.placeOrder(order);
        // release future, the triggeredOrder can be answered.
        PlaceOrderResponse response = new PlaceOrderResponse(order.getClientOrderId(), order.getOrderId());
        commandEvent.getFuture().complete(ResponseHelper.success(response));
    }

    /**
     * cancel order handler
     */
    private void cancelOrderHandler(CommandEvent<CancelOrderRequest> commandEvent) {
        long startTime = Utils.currentMicroTime();
        CancelOrderRequest request = commandEvent.getRequest();
        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.CANCEL_ORDER);
        Order order;
        Lock lock = ClientLockHelper.getLock(request.getClientId());
        lock.lock();
        try {
            // get and init session
            OrderSession session = OrderSessionHelper.get();
            session.init(request.getClientId(), request.getSymbol(), localCache, commandEvent.getRequestId());
            // execute trade rule
            for (TradeRule rule : tradeRules) {
                rule.cancelOrder(request, session);
            }
            // set the cancel order
            order = session.getOrder();
        } finally {
            lock.unlock();
        }

        if (order.getType() == OrderType.LIMIT || order.getType() == OrderType.MARKET) {
            // send to match engine
            MatchEngine matchEngine = matchEngineManager.getMatchEngine(request.getSymbol());
            matchEngine.cancelOrder(order);
        }
        // release future, the request can be answered.
        commandEvent.getFuture().complete(ResponseHelper.success());
        // statistics execute time
        if (StatisticsHelper.enabled()) {
            log.info("cancel order in commandEvent queue execute_time: {}us", Utils.currentMicroTime() - startTime);
        }
    }

    /**
     * cancel order when stop order is rejected
     */
    private void stopRejectedCancelOrderHandler(CommandEvent<Order> commandEvent) {
        Order cancelOrder = commandEvent.getRequest();
        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.CANCEL_ORDER);
        Lock lock = ClientLockHelper.getLock(cancelOrder.getClientId());
        lock.lock();
        try {
            // get and init session
            OrderSession session = OrderSessionHelper.get();
            session.init(cancelOrder.getClientId(), cancelOrder.getSymbol(), localCache, commandEvent.getRequestId());
            session.setStopRejected(true);

            CancelOrderRequest request = beanMapping.convertCancel(cancelOrder);
            // execute trade rule
            for (TradeRule rule : tradeRules) {
                rule.cancelOrder(request, session);
            }
        } finally {
            lock.unlock();
        }
        // release future, the request can be answered.
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * cancel order handler when liquidation
     */
    private void liquidationCancelOrderHandler(CommandEvent<LiquidationCancelOrderRequest> commandEvent) {
        LiquidationCancelOrderRequest request = commandEvent.getRequest();
        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.LIQUIDATION_CANCEL_ORDER);
        Order order;
        Lock lock = ClientLockHelper.getLock(request.getClientId());
        lock.lock();
        try {
            // get and init session
            OrderSession session = OrderSessionHelper.get();
            session.init(request.getClientId(), request.getSymbol(), localCache, commandEvent.getRequestId());
            // check if tradeId has changed
            liquidationCheckTradeId(session.getClientEntity(), request.getExpectedTradeId());
            // execute trade rule
            for (TradeRule rule : tradeRules) {
                rule.cancelOrder(request, session);
            }
            // set the cancel order
            order = session.getOrder();
        } finally {
            lock.unlock();
        }

        // send to match engine
        MatchEngine matchEngine = matchEngineManager.getMatchEngine(request.getSymbol());
        matchEngine.liquidationCancelOrder(new LiquidationCanceledOrder(request.getLatch(), order));
        // release future, the request can be answered.
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * trigger secondary order handler
     */
    private void triggerSecondaryOrderHandler(CommandEvent<Order> commandEvent) {
        Order secondaryOrder = commandEvent.getRequest();
        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.TRIGGER_SECONDARY_ORDER);
        Lock lock = ClientLockHelper.getLock(secondaryOrder.getClientId());
        lock.lock();
        try {
            // get and init session
            OrderSession session = OrderSessionHelper.get();
            session.init(secondaryOrder.getClientId(), secondaryOrder.getSymbol(), localCache,
                commandEvent.getRequestId());
            // execute trade rule
            for (TradeRule rule : tradeRules) {
                rule.triggerSecondaryOrder(commandEvent.getRequestId(), secondaryOrder, session);
            }
        } finally {
            lock.unlock();
        }
        // release future, the request can be answered.
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * OTC trade handler
     */
    private void otcTradeHandler(CommandEvent<OTCRequest> commandEvent) {
        OTCRequest request = commandEvent.getRequest();
        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.OTC_TRADE);
        if (!CollectionUtils.isEmpty(request.getDetailList())) {
            Lock lock1 = ClientLockHelper.getLock(request.getClientId1());
            lock1.lock();
            Lock lock2 = ClientLockHelper.getLock(request.getClientId2());
            lock2.lock();
            try {
                // check if tradeId changed
                ClientEntity clientEntity1 = localCache.getClientEntity(request.getClientId1());
                liquidationCheckTradeId(clientEntity1, request.getExpectedTradeId1());
                ClientEntity clientEntity2 = localCache.getClientEntity(request.getClientId2());
                liquidationCheckTradeId(clientEntity2, request.getExpectedTradeId2());
                // execute OTC trade
                for (TradeRule rule : tradeRules) {
                    rule.otcTrade(commandEvent.getRequestId(), request);
                }
            } finally {
                lock2.unlock();
                lock1.unlock();
            }
        }
        // release future, the request can be answered.
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * adjust position margin handler
     */
    private void adjustPositionMarginHandler(CommandEvent<AdjustPositionMarginRequest> commandEvent) {
        AdjustPositionMarginRequest request = commandEvent.getRequest();
        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.ADJUST_POSITION_MARGIN);
        Lock lock = ClientLockHelper.getLock(request.getClientId());
        lock.lock();
        try {
            for (TradeRule rule : tradeRules) {
                rule.adjustPositionMargin(commandEvent.getRequestId(), request);
            }
        } finally {
            lock.unlock();
        }
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * deduct collateral assets handler
     */
    private void deductCollateralHandler(CommandEvent<String> commandEvent) {
        String clientId = commandEvent.getRequest();
        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.DEDUCT_COLLATERAL);
        Lock lock = ClientLockHelper.getLock(clientId);
        lock.lock();
        try {
            for (TradeRule rule : tradeRules) {
                rule.deductCollateral(commandEvent.getRequestId(), clientId);
            }
        } finally {
            lock.unlock();
        }
        commandEvent.getFuture().complete(ResponseHelper.success());
    }

    /**
     * check tradeId when liquidation
     */
    private void liquidationCheckTradeId(ClientEntity clientEntity, Long expectedTradeId) {
        if (expectedTradeId != null && !expectedTradeId.equals(clientEntity.getLastTradeId())) {
            throw new TradeException(TradeError.HAS_OTHER_TRADE);
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
     * init the tradeQueue
     */
    private void initTradeQueue() {
        this.tradeDisruptor = DisruptorBuilder.<CommandEvent>newInstance().setRingBufferSize(tradeBufferSize)
            .setEventFactory(new CommandFactory())
            .setThreadFactory(new NamedThreadFactory("jtrade-trade-disruptor-", true))
            .setProducerType(ProducerType.MULTI).setWaitStrategy(new BlockingWaitStrategy()).build();
        this.tradeDisruptor.handleEventsWith(new JCommandHandler());
        this.tradeDisruptor
            .setDefaultExceptionHandler(new CommandLogExceptionHandler<CommandEvent>(getClass().getSimpleName()));
        this.tradeQueue = this.tradeDisruptor.start();
    }

    /**
     * check symbol status
     */
    private void checkSymbolStatus(SymbolInfo newSymbol) {
        SymbolInfo oldSymbol = localCache.getSymbolInfo(newSymbol.getSymbol());
        if (oldSymbol == null) {
            if (newSymbol.getStatus() != SymbolStatus.NO_TRADING) {
                throw new TradeException(TradeError.SYMBOL_STATUS_INVALID);
            }
        } else {
            if (oldSymbol.getStatus() != newSymbol.getStatus()) {
                throw new TradeException(TradeError.SYMBOL_STATUS_INVALID);
            }
        }
    }

    /**
     * check OTO orders
     */
    private Map<OTOOrderType, List<PlaceOrderRequest>> checkOTOOrders(List<PlaceOrderRequest> requestList) {
        if (CollectionUtils.isEmpty(requestList) || requestList.size() < 2 || requestList.size() > 3) {
            throw new TradeException(TradeError.OTO_ORDERS_INVALID);
        }

        Map<OTOOrderType, List<PlaceOrderRequest>> requestMap =
            requestList.stream().collect(Collectors.groupingBy(PlaceOrderRequest::getOtoOrderType));
        List<PlaceOrderRequest> primaryOrders = requestMap.get(OTOOrderType.PRIMARY);
        if (CollectionUtils.isEmpty(primaryOrders)) {
            throw new TradeException(TradeError.OTO_PRIMARY_NOT_EXISTS);
        } else if (primaryOrders.size() != 1) {
            throw new TradeException(TradeError.OTO_PRIMARY_ORDERS_INVALID);
        } else if (primaryOrders.get(0).getReduceOnly()) {
            throw new TradeException(TradeError.OTO_PRIMARY_REDUCE_ONLY_INVALID);
        }

        PlaceOrderRequest primaryOrder = primaryOrders.get(0);
        List<PlaceOrderRequest> secondaryOrders = requestMap.get(OTOOrderType.SECONDARY);
        if (CollectionUtils.isEmpty(secondaryOrders)) {
            throw new TradeException(TradeError.OTO_SECONDARY_NOT_EXISTS);
        } else if (secondaryOrders.size() > 2) {
            throw new TradeException(TradeError.OTO_SECONDARY_ORDERS_INVALID);
        } else {
            int stopCount = 0;
            int takeProfitCount = 0;
            for (PlaceOrderRequest order : secondaryOrders) {
                if (!order.getReduceOnly()) {
                    throw new TradeException(TradeError.OTO_SECONDARY_REDUCE_ONLY_INVALID);
                }
                if (order.getType() != OrderType.STOP_MARKET && order.getType() != OrderType.TAKE_PROFIT_MARKET) {
                    throw new TradeException(TradeError.OTO_SECONDARY_MARKET_INVALID);
                }
                if (!order.getSymbol().equals(primaryOrder.getSymbol())) {
                    throw new TradeException(TradeError.OTO_SECONDARY_SYMBOL_INVALID);
                }
                if (order.getQuantity().compareTo(primaryOrder.getQuantity()) != 0) {
                    throw new TradeException(TradeError.OTO_SECONDARY_QUANTITY_INVALID);
                }
                if (order.getSide() == primaryOrder.getSide()) {
                    throw new TradeException(TradeError.OTO_SECONDARY_SIDE_INVALID);
                }

                if (order.getType() == OrderType.STOP_MARKET) {
                    stopCount++;
                } else if (order.getType() == OrderType.TAKE_PROFIT_MARKET) {
                    takeProfitCount++;
                }
            }
            if (stopCount > 1 || takeProfitCount > 1) {
                throw new TradeException(TradeError.OTO_SECONDARY_ORDERS_INVALID);
            }
        }
        return requestMap;
    }

}
