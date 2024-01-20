package com.crypto.jtrade.core.provider.service.landing.impl;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.crypto.jtrade.core.provider.model.landing.AdjustPositionMarginLanding;
import com.crypto.jtrade.core.provider.model.landing.ClientFeeRateLanding;
import com.crypto.jtrade.core.provider.model.landing.SystemParameterLanding;
import com.crypto.jtrade.core.provider.model.queue.LandingEvent;
import com.crypto.jtrade.core.provider.service.rabbitmq.BatchingService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.CommandIdentity;
import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.DataAction;
import com.crypto.jtrade.common.constants.DataObject;
import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.constants.TradeType;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.Bill;
import com.crypto.jtrade.common.model.FeeRate;
import com.crypto.jtrade.common.model.TradeAuthority;
import com.crypto.jtrade.common.util.DisruptorBuilder;
import com.crypto.jtrade.common.util.LogExceptionHandler;
import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.common.util.TimerManager;
import com.crypto.jtrade.core.provider.config.CoreConfig;
import com.crypto.jtrade.core.provider.model.landing.AssetInfoLanding;
import com.crypto.jtrade.core.provider.model.landing.CancelOrderLanding;
import com.crypto.jtrade.core.provider.model.landing.ClientSettingLanding;
import com.crypto.jtrade.core.provider.model.landing.ClientTradeAuthorityLanding;
import com.crypto.jtrade.core.provider.model.landing.DeductCollateralLanding;
import com.crypto.jtrade.core.provider.model.landing.DepositLanding;
import com.crypto.jtrade.core.provider.model.landing.FundingFeeLanding;
import com.crypto.jtrade.core.provider.model.landing.MatchedLandings;
import com.crypto.jtrade.core.provider.model.landing.MySqlOperation;
import com.crypto.jtrade.core.provider.model.landing.OrderCanceledLanding;
import com.crypto.jtrade.core.provider.model.landing.OrderMatchedLanding;
import com.crypto.jtrade.core.provider.model.landing.PlaceOrderLanding;
import com.crypto.jtrade.core.provider.model.landing.SymbolInfoLanding;
import com.crypto.jtrade.core.provider.model.landing.WithdrawLanding;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.landing.MySqlLanding;
import com.crypto.jtrade.core.provider.service.landing.RedisLanding;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import lombok.extern.slf4j.Slf4j;

/**
 * save data to mysql asynchronously, send data to message queue.
 *
 * @author 0xWill
 **/
@Service(value = "mySqlLanding")
@Slf4j
public class MySqlLandingImpl extends BatchingService implements MySqlLanding {

    @Value("${jtrade.disruptor.mysql-buffer-size:8192}")
    private Integer mysqlBufferSize;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private RedisLanding redisLanding;

    private CoreConfig coreConfig;

    private Long rabbitBatchId;

    private Disruptor<LandingEvent> mysqlDisruptor;

    private RingBuffer<LandingEvent> mysqlQueue;

    @Autowired
    public MySqlLandingImpl(RabbitTemplate rabbitTemplate, CoreConfig coreConfig) {
        super(rabbitTemplate, coreConfig.getMysqlLandingMaxBatchSize(), Constants.MQ_EXCHANGE_MYSQL,
            Constants.MQ_ROUTING_MYSQL);
        this.coreConfig = coreConfig;
    }

    @PostConstruct
    public void init() {
        /**
         * init disruptor
         */
        this.mysqlDisruptor = DisruptorBuilder.<LandingEvent>newInstance().setRingBufferSize(mysqlBufferSize)
            .setEventFactory(new LandingFactory())
            .setThreadFactory(new NamedThreadFactory("jtrade-mySql-disruptor-", true))
            .setProducerType(ProducerType.MULTI).setWaitStrategy(new BlockingWaitStrategy()).build();
        this.mysqlDisruptor.handleEventsWith(new JLandingHandler());
        this.mysqlDisruptor.setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
        this.mysqlQueue = this.mysqlDisruptor.start();

        /**
         * init rabbitBatchId
         */
        String batchId = localCache.getSystemParameter(SystemParameter.LAST_MYSQL_BATCH_ID);
        rabbitBatchId = batchId == null ? 0L : Long.parseLong(batchId);

        initMySqlLandingTimer();
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
        this.mysqlDisruptor.publishEvent(translator);
    }

    /**
     * get the batch id
     */
    @Override
    protected Long getRabbitBatchId() {
        // increase by 1
        Long batchId = ++rabbitBatchId;
        // update local cache
        localCache.setSystemParameter(SystemParameter.LAST_MYSQL_BATCH_ID, batchId.toString());
        // write to redis
        com.crypto.jtrade.common.model.SystemParameter parameter = new com.crypto.jtrade.common.model.SystemParameter();
        parameter.setParameter(SystemParameter.LAST_MYSQL_BATCH_ID);
        parameter.setValue(batchId.toString());
        redisLanding.setSystemParameter(new SystemParameterLanding(null, parameter));
        return batchId;
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
                case MYSQL_LANDING:
                    mysqlLandingHandler();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * set system parameter handler
     */
    private void setSystemParameterHandler(SystemParameterLanding landing) {
        MySqlOperation operation = new MySqlOperation(Constants.EMPTY, DataObject.SYSTEM_PARAMETER, DataAction.UNKNOWN,
            landing.getParameter().toString());
        addToBatch(operation, true);
    }

    /**
     * set symbol info handler
     */
    private void setSymbolInfoHandler(SymbolInfoLanding landing) {
        MySqlOperation operation = new MySqlOperation(Constants.EMPTY, DataObject.SYMBOL_INFO, DataAction.UNKNOWN,
            landing.getSymbolInfo().toString());
        addToBatch(operation, true);
    }

    /**
     * set asset info handler
     */
    private void setAssetInfoHandler(AssetInfoLanding landing) {
        MySqlOperation operation = new MySqlOperation(Constants.EMPTY, DataObject.ASSET_INFO, DataAction.UNKNOWN,
            landing.getAssetInfo().toString());
        addToBatch(operation, true);
    }

    /**
     * set client fee rate handler
     */
    private void setClientFeeRateHandler(ClientFeeRateLanding landing) {
        for (FeeRate feeRate : landing.getFeeRateList()) {
            MySqlOperation operation = new MySqlOperation(feeRate.getClientId(), DataObject.CLIENT_FEE_RATE,
                DataAction.UNKNOWN, feeRate.toString());
            addToBatch(operation, true);
        }
    }

    /**
     * set client trade authority handler
     */
    private void setClientTradeAuthorityHandler(ClientTradeAuthorityLanding landing) {
        for (TradeAuthority tradeAuthority : landing.getTradeAuthorityList()) {
            MySqlOperation operation = new MySqlOperation(tradeAuthority.getClientId(), DataObject.CLIENT_AUTHORITY,
                DataAction.UNKNOWN, tradeAuthority.toString());
            addToBatch(operation, true);
        }
    }

    /**
     * set client setting handler
     */
    private void setClientSettingHandler(ClientSettingLanding landing) {
        MySqlOperation operation = new MySqlOperation(landing.getClientSetting().getClientId(),
            DataObject.CLIENT_SETTING, DataAction.UNKNOWN, landing.getClientSetting().toString());
        addToBatch(operation, true);
    }

    /**
     * deposit handler
     */
    private void depositHandler(DepositLanding landing) {
        String clientId = landing.getBalance().getClientId();
        MySqlOperation balanceOperation = new MySqlOperation(clientId, DataObject.ASSET_BALANCE,
            landing.getBalanceAction(), landing.getBalance().toString());
        addToBatch(balanceOperation, false);

        MySqlOperation billOperation =
            new MySqlOperation(clientId, DataObject.BILL, DataAction.INSERT, landing.getBill().toString());
        addToBatch(billOperation, true);
    }

    /**
     * withdraw handler
     */
    private void withdrawHandler(WithdrawLanding landing) {
        String clientId = landing.getBalance().getClientId();
        // AssetBalance
        MySqlOperation balanceOperation = new MySqlOperation(clientId, DataObject.ASSET_BALANCE,
            landing.getBalanceAction(), landing.getBalance().toString());
        addToBatch(balanceOperation, false);
        // Bill
        MySqlOperation billOperation =
            new MySqlOperation(clientId, DataObject.BILL, DataAction.INSERT, landing.getBill().toString());
        addToBatch(billOperation, true);
    }

    /**
     * place order handler
     */
    private void placeOrderHandler(PlaceOrderLanding landing) {
        String clientId = landing.getOrder().getClientId();
        // Position
        if (landing.getPosition() != null) {
            MySqlOperation positionOperation = new MySqlOperation(clientId, DataObject.POSITION,
                landing.getPositionAction(), landing.getPosition().toString());
            addToBatch(positionOperation, false);
        }
        // AssetBalance
        if (landing.getBalance() != null) {
            MySqlOperation balanceOperation = new MySqlOperation(clientId, DataObject.ASSET_BALANCE,
                landing.getBalanceAction(), landing.getBalance().toString());
            addToBatch(balanceOperation, false);
        }
        // Order
        MySqlOperation orderOperation =
            new MySqlOperation(clientId, DataObject.ORDER, DataAction.INSERT, landing.getOrder().toString());
        addToBatch(orderOperation, true);
    }

    /**
     * cancel order handler
     */
    private void cancelOrderHandler(CancelOrderLanding landing) {
        MySqlOperation orderOperation = new MySqlOperation(landing.getOrder().getClientId(), DataObject.ORDER,
            DataAction.DELETE, landing.getOrder().toString());
        addToBatch(orderOperation, true);
    }

    /**
     * order canceled handler
     */
    private void orderCanceledHandler(OrderCanceledLanding landing) {
        String clientId = landing.getOrder().getClientId();
        // Order
        MySqlOperation orderOperation =
            new MySqlOperation(clientId, DataObject.ORDER, DataAction.DELETE, landing.getOrder().toString());
        addToBatch(orderOperation, false);
        // Position
        MySqlOperation positionOperation = new MySqlOperation(clientId, DataObject.POSITION,
            landing.getPositionAction(), landing.getPosition().toString());
        addToBatch(positionOperation, false);
        // AssetBalance
        MySqlOperation balanceOperation =
            new MySqlOperation(clientId, DataObject.ASSET_BALANCE, DataAction.UPDATE, landing.getBalance().toString());
        addToBatch(balanceOperation, true);
    }

    /**
     * order matched handler
     */
    private void orderMatchedHandler(MatchedLandings landing) {
        orderMatchedHandler(landing.getBuyLanding());
        orderMatchedHandler(landing.getSellLanding());
        trySendData();
    }

    /**
     * order matched handler
     */
    private void orderMatchedHandler(OrderMatchedLanding landing) {
        String clientId = landing.getTrade().getClientId();
        // Order
        if (landing.getTradeType() == TradeType.STANDARD) {
            MySqlOperation orderOperation =
                new MySqlOperation(clientId, DataObject.ORDER, landing.getOrderAction(), landing.getOrder().toString());
            addToBatch(orderOperation, false);
        }
        // Position
        MySqlOperation positionOperation = new MySqlOperation(clientId, DataObject.POSITION,
            landing.getPositionAction(), landing.getPosition().toString());
        addToBatch(positionOperation, false);
        // AssetBalance
        MySqlOperation balanceOperation = new MySqlOperation(clientId, DataObject.ASSET_BALANCE,
            landing.getBalanceAction(), landing.getBalance().toString());
        addToBatch(balanceOperation, false);
        // Trade
        MySqlOperation tradeOperation =
            new MySqlOperation(clientId, DataObject.TRADE, DataAction.INSERT, landing.getTrade().toString());
        addToBatch(tradeOperation, false);
        // realized profit
        if (landing.getProfitBill() != null) {
            MySqlOperation profitOperation =
                new MySqlOperation(clientId, DataObject.BILL, DataAction.INSERT, landing.getProfitBill().toString());
            addToBatch(profitOperation, false);
        }
        // fee
        MySqlOperation feeOperation =
            new MySqlOperation(clientId, DataObject.BILL, DataAction.INSERT, landing.getFeeBill().toString());
        addToBatch(feeOperation, false);
    }

    /**
     * set funding fee handler
     */
    private void setFundingFeeHandler(FundingFeeLanding landing) {
        for (AssetBalance balance : landing.getBalances()) {
            MySqlOperation operation = new MySqlOperation(balance.getClientId(), DataObject.ASSET_BALANCE,
                DataAction.UPDATE, balance.toString());
            addToBatch(operation, true);
        }
        for (Bill bill : landing.getBills()) {
            MySqlOperation operation =
                new MySqlOperation(bill.getClientId(), DataObject.BILL, DataAction.INSERT, bill.toString());
            addToBatch(operation, true);
        }
    }

    /**
     * adjust position margin handler
     */
    private void adjustPositionMarginHandler(AdjustPositionMarginLanding landing) {
        String clientId = landing.getBalance().getClientId();
        // Position
        MySqlOperation positionOperation =
            new MySqlOperation(clientId, DataObject.POSITION, DataAction.UPDATE, landing.getPosition().toString());
        addToBatch(positionOperation, false);
        // AssetBalance
        MySqlOperation balanceOperation =
            new MySqlOperation(clientId, DataObject.ASSET_BALANCE, DataAction.UPDATE, landing.getBalance().toString());
        addToBatch(balanceOperation, true);
    }

    /**
     * deduct collateral handler
     */
    private void deductCollateralHandler(DeductCollateralLanding landing) {
        // sell side
        String sellClientId = landing.getSellClientId();
        for (AssetBalance balance : landing.getSellBalanceList()) {
            MySqlOperation operation =
                new MySqlOperation(sellClientId, DataObject.ASSET_BALANCE, DataAction.UPDATE, balance.toString());
            addToBatch(operation, false);
        }
        for (Bill bill : landing.getSellBillList()) {
            MySqlOperation operation =
                new MySqlOperation(sellClientId, DataObject.BILL, DataAction.INSERT, bill.toString());
            addToBatch(operation, false);
        }
        // buy side
        String buyClientId = landing.getBuyClientId();
        for (Map.Entry<AssetBalance, DataAction> entry : landing.getBuyBalanceMap().entrySet()) {
            MySqlOperation operation =
                new MySqlOperation(buyClientId, DataObject.ASSET_BALANCE, entry.getValue(), entry.getKey().toString());
            addToBatch(operation, false);
        }
        for (Bill bill : landing.getBuyBillList()) {
            MySqlOperation operation =
                new MySqlOperation(buyClientId, DataObject.BILL, DataAction.INSERT, bill.toString());
            addToBatch(operation, false);
        }
    }

    /**
     * MYSQL_LANDING handler
     */
    private void mysqlLandingHandler() {
        sendData();
    }

    /**
     * add to batch
     */
    private void addToBatch(MySqlOperation operation, boolean trySend) {
        addToBatch(operation.toString(), trySend);
    }

    /**
     * init mysql landing timer
     */
    private void initMySqlLandingTimer() {
        int interval = coreConfig.getMysqlLandingIntervalMilliSeconds();
        TimerManager.scheduleAtFixedRate(() -> onTimeMySqlLanding(), interval, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * time to mysql landing
     */
    private void onTimeMySqlLanding() {
        try {
            publishToQueue(CommandIdentity.MYSQL_LANDING, null);
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
