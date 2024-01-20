package com.crypto.jtrade.core.provider.service.landing.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.CommandIdentity;
import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.DataAction;
import com.crypto.jtrade.common.constants.OTOOrderType;
import com.crypto.jtrade.common.constants.RedisOp;
import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.constants.TradeType;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.util.DisruptorBuilder;
import com.crypto.jtrade.common.util.LogExceptionHandler;
import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.common.util.TimerManager;
import com.crypto.jtrade.common.util.Utils;
import com.crypto.jtrade.core.provider.config.CoreConfig;
import com.crypto.jtrade.core.provider.model.landing.AdjustPositionMarginLanding;
import com.crypto.jtrade.core.provider.model.landing.AssetInfoLanding;
import com.crypto.jtrade.core.provider.model.landing.CancelOrderLanding;
import com.crypto.jtrade.core.provider.model.landing.ClientFeeRateLanding;
import com.crypto.jtrade.core.provider.model.landing.ClientSettingLanding;
import com.crypto.jtrade.core.provider.model.landing.ClientTradeAuthorityLanding;
import com.crypto.jtrade.core.provider.model.landing.DeductCollateralLanding;
import com.crypto.jtrade.core.provider.model.landing.DepositLanding;
import com.crypto.jtrade.core.provider.model.landing.FundingFeeLanding;
import com.crypto.jtrade.core.provider.model.landing.MatchedLandings;
import com.crypto.jtrade.core.provider.model.landing.OrderCanceledLanding;
import com.crypto.jtrade.core.provider.model.landing.OrderMatchedLanding;
import com.crypto.jtrade.core.provider.model.landing.PlaceOrderLanding;
import com.crypto.jtrade.core.provider.model.landing.RedisOperation;
import com.crypto.jtrade.core.provider.model.landing.SymbolInfoLanding;
import com.crypto.jtrade.core.provider.model.landing.SystemParameterLanding;
import com.crypto.jtrade.core.provider.model.landing.WithdrawLanding;
import com.crypto.jtrade.core.provider.model.queue.LandingEvent;
import com.crypto.jtrade.core.provider.service.cache.RedisService;
import com.crypto.jtrade.core.provider.service.landing.RedisLanding;
import com.crypto.jtrade.core.provider.util.SequenceHelper;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import lombok.extern.slf4j.Slf4j;

/**
 * save data to redis asynchronously
 *
 * @author 0xWill
 **/
@Service(value = "redisLanding")
@Slf4j
public class RedisLandingImpl implements RedisLanding {

    @Value("${jtrade.disruptor.redis-buffer-size:8192}")
    private Integer redisBufferSize;

    @Autowired
    private CoreConfig coreConfig;

    @Autowired
    private RedisService redisService;

    /**
     * The initial capacity of ArrayList is related to MAX_BATCH_COUNT.
     */
    private List<RedisOperation> operationList = new ArrayList<>(128);

    private Disruptor<LandingEvent> redisDisruptor;

    private RingBuffer<LandingEvent> redisQueue;

    @PostConstruct
    public void init() {
        this.redisDisruptor = DisruptorBuilder.<LandingEvent>newInstance().setRingBufferSize(redisBufferSize)
            .setEventFactory(new LandingFactory())
            .setThreadFactory(new NamedThreadFactory("jtrade-redis-disruptor-", true))
            .setProducerType(ProducerType.MULTI).setWaitStrategy(new BlockingWaitStrategy()).build();
        this.redisDisruptor.handleEventsWith(new JLandingHandler());
        this.redisDisruptor.setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
        this.redisQueue = this.redisDisruptor.start();

        initRedisLandingTimer();
    }

    /**
     * set system parameter
     */
    @Override
    public void setSystemParameter(SystemParameterLanding landing) {
        publishToQueue(CommandIdentity.SET_SYSTEM_PARAMETER, landing);
    }

    /**
     * set symbol info
     */
    @Override
    public void setSymbolInfo(SymbolInfoLanding landing) {
        publishToQueue(CommandIdentity.SET_SYMBOL_INFO, landing);
    }

    /**
     * set asset info
     */
    @Override
    public void setAssetInfo(AssetInfoLanding landing) {
        publishToQueue(CommandIdentity.SET_ASSET_INFO, landing);
    }

    /**
     * set client fee rate
     */
    @Override
    public void setClientFeeRate(ClientFeeRateLanding landing) {
        publishToQueue(CommandIdentity.SET_CLIENT_FEE_RATE, landing);
    }

    /**
     * set client trade authority
     */
    @Override
    public void setClientTradeAuthority(ClientTradeAuthorityLanding landing) {
        publishToQueue(CommandIdentity.SET_CLIENT_TRADE_AUTHORITY, landing);
    }

    /**
     * set client setting
     */
    @Override
    public void setClientSetting(ClientSettingLanding landing) {
        publishToQueue(CommandIdentity.SET_CLIENT_SETTING, landing);
    }

    /**
     * deposit
     */
    @Override
    public void deposit(DepositLanding landing) {
        publishToQueue(CommandIdentity.DEPOSIT, landing);
    }

    /**
     * withdraw
     */
    @Override
    public void withdraw(WithdrawLanding landing) {
        publishToQueue(CommandIdentity.WITHDRAW, landing);
    }

    /**
     * place order
     */
    @Override
    public void placeOrder(PlaceOrderLanding landing) {
        publishToQueue(CommandIdentity.PLACE_ORDER, landing);
    }

    /**
     * cancel order
     */
    @Override
    public void cancelOrder(CancelOrderLanding landing) {
        publishToQueue(CommandIdentity.CANCEL_ORDER, landing);
    }

    /**
     * order matched
     */
    @Override
    public void orderMatched(MatchedLandings landing) {
        publishToQueue(CommandIdentity.ORDER_MATCHED, landing);
    }

    /**
     * order canceled
     */
    @Override
    public void orderCanceled(OrderCanceledLanding landing) {
        publishToQueue(CommandIdentity.ORDER_CANCELED, landing);
    }

    /**
     * funding fee
     */
    @Override
    public void setFundingFee(FundingFeeLanding landing) {
        publishToQueue(CommandIdentity.SET_FUNDING_FEE, landing);
    }

    /**
     * adjust position margin
     */
    @Override
    public void adjustPositionMargin(AdjustPositionMarginLanding landing) {
        publishToQueue(CommandIdentity.ADJUST_POSITION_MARGIN, landing);
    }

    @Override
    public void deductCollateral(DeductCollateralLanding landing) {
        publishToQueue(CommandIdentity.DEDUCT_COLLATERAL, landing);
    }

    /**
     * publish to queue
     */
    private void publishToQueue(CommandIdentity identity, Object data) {
        final EventTranslator<LandingEvent> translator = (event, sequence) -> {
            event.setIdentity(identity);
            event.setData(data);
        };
        /**
         * FIXME: If the queue is full, publishEvent will be blocking, the system is blocked.
         */
        this.redisDisruptor.publishEvent(translator);
    }

    /**
     * JLanding handler for Disruptor
     */
    private class JLandingHandler implements EventHandler<LandingEvent> {

        @Override
        public void onEvent(final LandingEvent landingEvent, final long sequence, final boolean endOfBatch)
            throws Exception {
            switch (landingEvent.getIdentity()) {
                case SET_SYSTEM_PARAMETER:
                    setSystemParameterHandler((SystemParameterLanding)landingEvent.getData());
                    break;
                case SET_SYMBOL_INFO:
                    setSymbolInfoHandler((SymbolInfoLanding)landingEvent.getData());
                    break;
                case SET_ASSET_INFO:
                    setAssetInfoHandler((AssetInfoLanding)landingEvent.getData());
                    break;
                case SET_CLIENT_FEE_RATE:
                    setClientFeeRateHandler((ClientFeeRateLanding)landingEvent.getData());
                    break;
                case SET_CLIENT_TRADE_AUTHORITY:
                    setClientTradeAuthorityHandler((ClientTradeAuthorityLanding)landingEvent.getData());
                    break;
                case SET_CLIENT_SETTING:
                    setClientSettingHandler((ClientSettingLanding)landingEvent.getData());
                    break;
                case DEPOSIT:
                    depositHandler((DepositLanding)landingEvent.getData());
                    break;
                case WITHDRAW:
                    withdrawHandler((WithdrawLanding)landingEvent.getData());
                    break;
                case PLACE_ORDER:
                    placeOrderHandler((PlaceOrderLanding)landingEvent.getData());
                    break;
                case CANCEL_ORDER:
                    cancelOrderHandler((CancelOrderLanding)landingEvent.getData());
                    break;
                case ORDER_MATCHED:
                    orderMatchedHandler((MatchedLandings)landingEvent.getData());
                    break;
                case ORDER_CANCELED:
                    orderCanceledHandler((OrderCanceledLanding)landingEvent.getData());
                    break;
                case SET_FUNDING_FEE:
                    setFundingFeeHandler((FundingFeeLanding)landingEvent.getData());
                    break;
                case ADJUST_POSITION_MARGIN:
                    adjustPositionMarginHandler((AdjustPositionMarginLanding)landingEvent.getData());
                    break;
                case DEDUCT_COLLATERAL:
                    deductCollateralHandler((DeductCollateralLanding)landingEvent.getData());
                    break;
                case REDIS_LANDING:
                    redisLandingHandler();
                    break;
                default:
                    break;
            }
            /**
             * write to redis
             */
            tryWriteToRedis();
        }
    }

    /**
     * set system parameter handler
     */
    private void setSystemParameterHandler(SystemParameterLanding landing) {
        if (landing.getRequestId() != null) {
            operationList.add(
                new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER, SystemParameter.LAST_REQUEST_ID.toString(),
                    String.valueOf(landing.getRequestId()), false, RedisOp.HASH));
        }
        operationList.add(new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER,
            landing.getParameter().getParameter().toString(), landing.getParameter().getValue(), false, RedisOp.HASH));
    }

    /**
     * set symbol info handler
     */
    private void setSymbolInfoHandler(SymbolInfoLanding landing) {
        if (landing.getRequestId() != null) {
            operationList.add(
                new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER, SystemParameter.LAST_REQUEST_ID.toString(),
                    String.valueOf(landing.getRequestId()), false, RedisOp.HASH));
        }
        operationList.add(new RedisOperation(Constants.REDIS_KEY_SYMBOL, landing.getSymbolInfo().getSymbol(),
            landing.getSymbolInfo().toString(), false, RedisOp.HASH));
    }

    /**
     * set asset info handler
     */
    private void setAssetInfoHandler(AssetInfoLanding landing) {
        if (landing.getRequestId() != null) {
            operationList.add(
                new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER, SystemParameter.LAST_REQUEST_ID.toString(),
                    String.valueOf(landing.getRequestId()), false, RedisOp.HASH));
        }
        operationList.add(new RedisOperation(Constants.REDIS_KEY_ASSET, landing.getAssetInfo().getAsset(),
            landing.getAssetInfo().toString(), false, RedisOp.HASH));
    }

    /**
     * set client fee rate handler
     */
    private void setClientFeeRateHandler(ClientFeeRateLanding landing) {
        operationList.add(new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER,
            SystemParameter.LAST_REQUEST_ID.toString(), String.valueOf(landing.getRequestId()), false, RedisOp.HASH));
        landing.getFeeRateList().forEach(feeRate -> {
            String redisKey = Utils.format(Constants.REDIS_KEY_CLIENT_FEE_RATE, feeRate.getClientId());
            operationList.add(new RedisOperation(redisKey, null, feeRate.toString(), false, RedisOp.VALUE));
        });
    }

    /**
     * set client trade authority handler
     */
    private void setClientTradeAuthorityHandler(ClientTradeAuthorityLanding landing) {
        operationList.add(new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER,
            SystemParameter.LAST_REQUEST_ID.toString(), String.valueOf(landing.getRequestId()), false, RedisOp.HASH));
        landing.getTradeAuthorityList().forEach(authority -> {
            String redisKey = Utils.format(Constants.REDIS_KEY_CLIENT_AUTHORITY, authority.getClientId());
            operationList.add(new RedisOperation(redisKey, null, String.valueOf(authority.getTradeAuthority()), false,
                RedisOp.VALUE));
        });
    }

    /**
     * set client setting handler
     */
    private void setClientSettingHandler(ClientSettingLanding landing) {
        String redisKey = Utils.format(Constants.REDIS_KEY_CLIENT_SETTING, landing.getClientSetting().getClientId());
        operationList.add(new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER,
            SystemParameter.LAST_REQUEST_ID.toString(), String.valueOf(landing.getRequestId()), false, RedisOp.HASH));
        operationList.add(new RedisOperation(redisKey, landing.getClientSetting().getSymbol(),
            landing.getClientSetting().toString(), false, RedisOp.HASH));
    }

    /**
     * deposit handler
     */
    private void depositHandler(DepositLanding landing) {
        String redisKey = Utils.format(Constants.REDIS_KEY_BALANCE, landing.getBalance().getClientId());
        operationList.add(new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER,
            SystemParameter.LAST_REQUEST_ID.toString(), String.valueOf(landing.getRequestId()), false, RedisOp.HASH));
        operationList.add(new RedisOperation(redisKey, landing.getBalance().getAsset(), landing.getBalance().toString(),
            false, RedisOp.HASH));
        if (landing.getDebtClientAction() != DataAction.NONE) {
            operationList.add(new RedisOperation(Constants.REDIS_KEY_DEBT_CLIENTS, null,
                landing.getBalance().getClientId(), landing.getDebtClientAction() == DataAction.DELETE, RedisOp.SET));
        }
    }

    /**
     * withdraw handler
     */
    private void withdrawHandler(WithdrawLanding landing) {
        String redisKey = Utils.format(Constants.REDIS_KEY_BALANCE, landing.getBalance().getClientId());
        operationList.add(new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER,
            SystemParameter.LAST_REQUEST_ID.toString(), String.valueOf(landing.getRequestId()), false, RedisOp.HASH));
        operationList.add(new RedisOperation(redisKey, landing.getBalance().getAsset(), landing.getBalance().toString(),
            false, RedisOp.HASH));
    }

    /**
     * place order handler
     */
    private void placeOrderHandler(PlaceOrderLanding landing) {
        operationList.add(new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER,
            SystemParameter.LAST_REQUEST_ID.toString(), String.valueOf(landing.getRequestId()), false, RedisOp.HASH));
        if (!landing.isStopTriggered() && landing.getOrder().getOtoOrderType() != OTOOrderType.SECONDARY) {
            operationList
                .add(new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER, SystemParameter.LAST_ORDER_ID.toString(),
                    String.valueOf(landing.getOrder().getOrderId()), false, RedisOp.HASH));
        }

        String redisKeyOrder = Utils.format(Constants.REDIS_KEY_ORDER, landing.getOrder().getClientId());
        operationList.add(new RedisOperation(redisKeyOrder, String.valueOf(landing.getOrder().getOrderId()),
            landing.getOrder().toString(), false, RedisOp.HASH));

        if (landing.getPosition() != null) {
            String redisKeyPosition = Utils.format(Constants.REDIS_KEY_POSITION, landing.getPosition().getClientId());
            operationList.add(new RedisOperation(redisKeyPosition, landing.getPosition().getSymbol(),
                landing.getPosition().toString(), false, RedisOp.HASH));
        }
        if (landing.getBalance() != null) {
            String redisKeyBalance = Utils.format(Constants.REDIS_KEY_BALANCE, landing.getBalance().getClientId());
            operationList.add(new RedisOperation(redisKeyBalance, landing.getBalance().getAsset(),
                landing.getBalance().toString(), false, RedisOp.HASH));
        }
        if (landing.getOrderClientAction() != DataAction.NONE) {
            operationList.add(new RedisOperation(Constants.REDIS_KEY_ORDER_CLIENTS, null,
                landing.getOrder().getClientId(), landing.getOrderClientAction() == DataAction.DELETE, RedisOp.SET));
        }
    }

    /**
     * cancel order handler
     */
    private void cancelOrderHandler(CancelOrderLanding landing) {
        if (landing.getRequestId() != null) {
            operationList.add(
                new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER, SystemParameter.LAST_REQUEST_ID.toString(),
                    String.valueOf(landing.getRequestId()), false, RedisOp.HASH));
        }

        String redisKeyOrder = Utils.format(Constants.REDIS_KEY_ORDER, landing.getOrder().getClientId());
        operationList.add(new RedisOperation(redisKeyOrder, String.valueOf(landing.getOrder().getOrderId()), null, true,
            RedisOp.HASH));

        if (landing.getOrderClientAction() != DataAction.NONE) {
            operationList.add(new RedisOperation(Constants.REDIS_KEY_ORDER_CLIENTS, null,
                landing.getOrder().getClientId(), landing.getOrderClientAction() == DataAction.DELETE, RedisOp.SET));
        }
    }

    /**
     * order canceled handler
     */
    private void orderCanceledHandler(OrderCanceledLanding landing) {
        String redisKeyOrder = Utils.format(Constants.REDIS_KEY_ORDER, landing.getOrder().getClientId());
        String redisKeyPosition = Utils.format(Constants.REDIS_KEY_POSITION, landing.getPosition().getClientId());
        String redisKeyBalance = Utils.format(Constants.REDIS_KEY_BALANCE, landing.getBalance().getClientId());

        operationList.add(new RedisOperation(redisKeyOrder, String.valueOf(landing.getOrder().getOrderId()), null, true,
            RedisOp.HASH));
        operationList.add(new RedisOperation(redisKeyPosition, landing.getPosition().getSymbol(),
            landing.getPosition().toString(), landing.getPositionAction() == DataAction.DELETE, RedisOp.HASH));
        operationList.add(new RedisOperation(redisKeyBalance, landing.getBalance().getAsset(),
            landing.getBalance().toString(), false, RedisOp.HASH));

        if (landing.getOrderClientAction() != DataAction.NONE) {
            operationList.add(new RedisOperation(Constants.REDIS_KEY_ORDER_CLIENTS, null,
                landing.getOrder().getClientId(), landing.getOrderClientAction() == DataAction.DELETE, RedisOp.SET));
        }
    }

    /**
     * order matched handler
     */
    private void orderMatchedHandler(MatchedLandings landing) {
        orderMatchedHandler(landing.getBuyLanding(), operationList);
        orderMatchedHandler(landing.getSellLanding(), operationList);
    }

    /**
     * order matched handler
     */
    private void orderMatchedHandler(OrderMatchedLanding landing, List<RedisOperation> operationList) {
        String redisKeyOrder = Utils.format(Constants.REDIS_KEY_ORDER, landing.getOrder().getClientId());
        String redisKeyPosition = Utils.format(Constants.REDIS_KEY_POSITION, landing.getPosition().getClientId());
        String redisKeyBalance = Utils.format(Constants.REDIS_KEY_BALANCE, landing.getBalance().getClientId());

        // Because the matching is parallel, the tradeId is directly taken.
        operationList
            .add(new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER, SystemParameter.LAST_TRADE_ID.toString(),
                String.valueOf(SequenceHelper.getTradeId()), false, RedisOp.HASH));
        if (landing.getTradeType() == TradeType.STANDARD) {
            operationList.add(new RedisOperation(redisKeyOrder, String.valueOf(landing.getOrder().getOrderId()),
                landing.getOrder().toString(), landing.getOrderAction() == DataAction.DELETE, RedisOp.HASH));
        }
        operationList.add(new RedisOperation(redisKeyPosition, landing.getPosition().getSymbol(),
            landing.getPosition().toString(), landing.getPositionAction() == DataAction.DELETE, RedisOp.HASH));
        operationList.add(new RedisOperation(redisKeyBalance, landing.getBalance().getAsset(),
            landing.getBalance().toString(), false, RedisOp.HASH));

        if (landing.getOrderClientAction() != DataAction.NONE) {
            operationList.add(new RedisOperation(Constants.REDIS_KEY_ORDER_CLIENTS, null,
                landing.getOrder().getClientId(), landing.getOrderClientAction() == DataAction.DELETE, RedisOp.SET));
        }
        if (landing.getPositionClientAction() != DataAction.NONE) {
            operationList.add(new RedisOperation(Constants.REDIS_KEY_POSITION_CLIENTS, null,
                landing.getOrder().getClientId(), landing.getPositionClientAction() == DataAction.DELETE, RedisOp.SET));
        }
        if (landing.getDebtClientAction() != DataAction.NONE) {
            operationList.add(new RedisOperation(Constants.REDIS_KEY_DEBT_CLIENTS, null,
                landing.getOrder().getClientId(), landing.getDebtClientAction() == DataAction.DELETE, RedisOp.SET));
        }
    }

    /**
     * set funding fee handler
     */
    private void setFundingFeeHandler(FundingFeeLanding landing) {
        operationList.add(new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER,
            SystemParameter.LAST_REQUEST_ID.toString(), String.valueOf(landing.getRequestId()), false, RedisOp.HASH));
        landing.getBalances().forEach(balance -> {
            String redisKey = Utils.format(Constants.REDIS_KEY_BALANCE, balance.getClientId());
            operationList
                .add(new RedisOperation(redisKey, balance.getAsset(), balance.toString(), false, RedisOp.HASH));
        });
    }

    /**
     * adjust position margin handler
     */
    private void adjustPositionMarginHandler(AdjustPositionMarginLanding landing) {
        String redisKeyPosition = Utils.format(Constants.REDIS_KEY_POSITION, landing.getPosition().getClientId());
        String redisKeyBalance = Utils.format(Constants.REDIS_KEY_BALANCE, landing.getBalance().getClientId());

        operationList.add(new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER,
            SystemParameter.LAST_REQUEST_ID.toString(), String.valueOf(landing.getRequestId()), false, RedisOp.HASH));
        operationList.add(new RedisOperation(redisKeyPosition, landing.getPosition().getSymbol(),
            landing.getPosition().toString(), false, RedisOp.HASH));
        operationList.add(new RedisOperation(redisKeyBalance, landing.getBalance().getAsset(),
            landing.getBalance().toString(), false, RedisOp.HASH));
    }

    /**
     * deduct collateral handler
     */
    private void deductCollateralHandler(DeductCollateralLanding landing) {
        operationList.add(new RedisOperation(Constants.REDIS_KEY_SYSTEM_PARAMETER,
            SystemParameter.LAST_REQUEST_ID.toString(), String.valueOf(landing.getRequestId()), false, RedisOp.HASH));
        // sell side
        String sellRedisKey = Utils.format(Constants.REDIS_KEY_BALANCE, landing.getSellClientId());
        for (AssetBalance balance : landing.getSellBalanceList()) {
            operationList
                .add(new RedisOperation(sellRedisKey, balance.getAsset(), balance.toString(), false, RedisOp.HASH));
        }
        // buy side
        String buyRedisKey = Utils.format(Constants.REDIS_KEY_BALANCE, landing.getBuyClientId());
        for (AssetBalance balance : landing.getBuyBalanceMap().keySet()) {
            operationList
                .add(new RedisOperation(buyRedisKey, balance.getAsset(), balance.toString(), false, RedisOp.HASH));
        }

        if (landing.getDebtClientAction() != DataAction.NONE) {
            operationList.add(new RedisOperation(Constants.REDIS_KEY_DEBT_CLIENTS, null, landing.getSellClientId(),
                landing.getDebtClientAction() == DataAction.DELETE, RedisOp.SET));
        }
    }

    /**
     * REDIS_LANDING handler
     */
    private void redisLandingHandler() {
        if (operationList.size() > 0) {
            redisService.batchWriteOperations(operationList, false);
            operationList.clear();
        }
    }

    /**
     * try writes to redis
     */
    private void tryWriteToRedis() {
        if (operationList.size() >= coreConfig.getRedisLandingMaxBatchSize()) {
            redisService.batchWriteOperations(operationList, false);
            operationList.clear();
        }
    }

    /**
     * init redis landing timer
     */
    private void initRedisLandingTimer() {
        int interval = coreConfig.getRedisLandingIntervalMilliSeconds();
        TimerManager.scheduleAtFixedRate(() -> onTimeRedisLanding(), interval, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * time to redis landing
     */
    private void onTimeRedisLanding() {
        try {
            publishToQueue(CommandIdentity.REDIS_LANDING, null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * LandingEvent factory for Disruptor
     */
    private static class LandingFactory implements EventFactory<LandingEvent> {

        @Override
        public LandingEvent newInstance() {
            return new LandingEvent();
        }
    }

}
