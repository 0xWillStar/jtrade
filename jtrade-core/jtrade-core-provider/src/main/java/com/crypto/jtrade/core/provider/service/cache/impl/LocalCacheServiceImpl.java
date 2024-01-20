package com.crypto.jtrade.core.provider.service.cache.impl;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.KlinePeriod;
import com.crypto.jtrade.common.constants.ProductType;
import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.AssetInfo;
import com.crypto.jtrade.common.model.Depth;
import com.crypto.jtrade.common.model.FeeRate;
import com.crypto.jtrade.common.model.Kline;
import com.crypto.jtrade.common.model.SymbolIndicator;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.model.Ticker;
import com.crypto.jtrade.common.util.concurrent.ConcurrentHashSet;
import com.crypto.jtrade.core.provider.config.CoreConfig;
import com.crypto.jtrade.core.provider.service.cache.ClientEntity;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.cache.RedisService;
import com.crypto.jtrade.core.provider.service.cache.StopOrderCache;
import com.crypto.jtrade.core.provider.util.SequenceHelper;
import com.crypto.jtrade.core.provider.util.StatisticsHelper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import lombok.extern.slf4j.Slf4j;

/**
 * Local cache service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class LocalCacheServiceImpl implements LocalCacheService {

    @Autowired
    private CoreConfig coreConfig;

    @Autowired
    private RedisService redisService;

    /**
     * System parameter cache
     */
    private ConcurrentHashMap<SystemParameter, String> systemParameters = new ConcurrentHashMap<>(32);

    /**
     * Symbol info cache, KEY：symbol
     */
    private ConcurrentHashMap<String, SymbolInfo> symbolCache = new ConcurrentHashMap<>(Constants.MAX_SYMBOL_COUNT);

    /**
     * Asset info cache, KEY: asset
     */
    private ConcurrentHashMap<String, AssetInfo> assetCache = new ConcurrentHashMap<>(Constants.MAX_SYMBOL_COUNT);

    /**
     * lock for update symbol
     */
    private ReentrantLock symbolWriteLock = new ReentrantLock();

    /**
     * Symbol indicator cache, KEY: symbol
     */
    private ConcurrentHashMap<String, SymbolIndicator> symbolIndicators =
        new ConcurrentHashMap<>(Constants.MAX_SYMBOL_COUNT);

    /**
     * Client cache, KEY：clientId
     */
    private LoadingCache<String, ClientEntity> clientCache;

    /**
     * cache all collaterals of asset, priced based on clear asset. KEY: clientId, VALUE: collateral
     */
    private LoadingCache<String, BigDecimal> clientCollateralCache;

    /**
     * market locks, KEY: symbol
     */
    private ConcurrentHashMap<String, ReentrantLock> marketLocks = new ConcurrentHashMap<>(Constants.MAX_SYMBOL_COUNT);

    /**
     * Depth cache, KEY: symbol
     */
    private ConcurrentHashMap<String, Depth> lastDepths = new ConcurrentHashMap<>(Constants.MAX_SYMBOL_COUNT);

    /**
     * Ticker cache, KEY: symbol
     */
    private ConcurrentHashMap<String, Ticker> lastTickers = new ConcurrentHashMap<>(Constants.MAX_SYMBOL_COUNT);

    /**
     * Kline cache, KEY: symbol
     */
    private ConcurrentHashMap<String, Map<KlinePeriod, Kline>> lastKlineCache =
        new ConcurrentHashMap<>(Constants.MAX_SYMBOL_COUNT);

    /**
     * Order clients set, VALUE: clientId
     */
    private ConcurrentHashSet<String> orderClientIds = new ConcurrentHashSet<>();

    /**
     * Position clients set, VALUE: clientId
     */
    private ConcurrentHashSet<String> positionClientIds = new ConcurrentHashSet<>();

    /**
     * client ids set which no positions and have debts, VALUE: clientId
     */
    private ConcurrentHashSet<String> debtClientIds = new ConcurrentHashSet<>();

    /**
     * Stop order cache, KEY: symbol
     */
    private ConcurrentHashMap<String, StopOrderCache> stopOrderCaches =
        new ConcurrentHashMap<>(Constants.MAX_SYMBOL_COUNT);

    @PostConstruct
    public void init() {
        clientCache = Caffeine.newBuilder().expireAfterAccess(Duration.ofHours(1))
            .maximumSize(coreConfig.getClientCacheSize()).build(key -> findClientEntity(key));
        clientCollateralCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofSeconds(5))
            .maximumSize(coreConfig.getClientCacheSize()).build(key -> findClientCollateral(key));

        // init system parameter cache, load all system parameters from redis
        systemParameters.putAll(redisService.getSystemParameters());
        // init symbol info cache, load all symbol info from redis
        symbolCache.putAll(redisService.getSymbols());
        // init asset info cache, load all asset info from redis
        assetCache.putAll(redisService.getAssets());

        // init requestId
        SequenceHelper.setRequestId(getSystemParameterOfLong(SystemParameter.LAST_REQUEST_ID));
        // init orderId
        SequenceHelper.setOrderId(getSystemParameterOfLong(SystemParameter.LAST_ORDER_ID));
        // init tradeId
        SequenceHelper.setTradeId(getSystemParameterOfLong(SystemParameter.LAST_TRADE_ID));
        // init statisticsEnabled
        StatisticsHelper.setStatisticsEnabled(getSystemParameterOfBoolean(SystemParameter.STATISTICS_ENABLED));

        initOrderClientIds();
        initPositionClientIds();
        initDebtClientIds();
        loadAllClientWithPosition();
    }

    @Override
    public ProductType getProductType() {
        return coreConfig.getProductType();
    }

    /**
     * get a system parameter from local cache
     */
    @Override
    public String getSystemParameter(SystemParameter parameter) {
        return systemParameters.get(parameter);
    }

    /**
     * set a system parameter into local cache
     */
    @Override
    public void setSystemParameter(SystemParameter parameter, String value) {
        systemParameters.put(parameter, value);
    }

    /**
     * get a symbol info instance from local cache
     */
    @Override
    public SymbolInfo getSymbolInfo(String symbol) {
        return symbolCache.get(symbol);
    }

    /**
     * get symbol write lock
     */
    @Override
    public ReentrantLock getSymbolWriteLock() {
        return symbolWriteLock;
    }

    /**
     * get all symbols from local cache
     */
    @Override
    public Map<String, SymbolInfo> getAllSymbols() {
        return symbolCache;
    }

    /**
     * set a symbol info into local cache
     */
    @Override
    public void setSymbolInfo(SymbolInfo symbol) {
        symbolCache.put(symbol.getSymbol(), symbol);
    }

    /**
     * get asset info from local cache
     */
    @Override
    public AssetInfo getAssetInfo(String asset) {
        return assetCache.get(asset);
    }

    /**
     * get all assets from local cache
     */
    @Override
    public Map<String, AssetInfo> getAllAssets() {
        return assetCache;
    }

    /**
     * set asset info into local cache
     */
    @Override
    public void setAssetInfo(AssetInfo assetInfo) {
        assetCache.put(assetInfo.getAsset(), assetInfo);
    }

    /**
     * get a symbol indicator from local cache
     */
    @Override
    public SymbolIndicator getSymbolIndicator(String symbol) {
        /**
         * The computeIfAbsent has poor performance, can't use it.
         */
        SymbolIndicator indicator = symbolIndicators.get(symbol);
        if (indicator == null) {
            indicator = new SymbolIndicator();
            symbolIndicators.put(symbol, indicator);
        }
        return indicator;
    }

    /**
     * get all symbol indicator
     */
    @Override
    public Map<String, SymbolIndicator> getSymbolIndicators() {
        return symbolIndicators;
    }

    /**
     * Get a client entity from local cache. If it does not exist in the local cache, load it from redis.
     */
    @Override
    public ClientEntity getClientEntity(String clientId) {
        return clientCache.get(clientId);
    }

    /**
     * get client collateral from local cache
     */
    @Override
    public BigDecimal getClientCollateral(String clientId) {
        return clientCollateralCache.get(clientId);
    }

    /**
     * invalidate client collateral cache
     */
    @Override
    public void invalidateClientCollateral(String clientId) {
        clientCollateralCache.invalidate(clientId);
    }

    /**
     * Tests if the specified object is a key in the marketLocks.
     */
    public boolean containsMarketLock(String symbol) {
        return marketLocks.containsKey(symbol);
    }

    /**
     * get market lock by the symbol
     */
    public ReentrantLock getMarketLock(String symbol) {
        return marketLocks.get(symbol);
    }

    /**
     * set market lock to the marketLocks
     */
    public void setMarketLock(String symbol, ReentrantLock lock) {
        marketLocks.put(symbol, lock);
    }

    /**
     * get last depth by the symbol
     */
    @Override
    public Depth getLastDepth(String symbol) {
        /**
         * The computeIfAbsent has poor performance, can't use it.
         */
        Depth depth = lastDepths.get(symbol);
        if (depth == null) {
            depth = new Depth(symbol, new ArrayList<>(), new ArrayList<>());
            lastDepths.put(symbol, depth);
        }
        return depth;
    }

    /**
     * set last depth to local cache
     */
    @Override
    public void setLastDepth(String symbol, Depth depth) {
        lastDepths.put(symbol, depth);
    }

    /**
     * get last ticker by the symbol
     */
    @Override
    public Ticker getLastTicker(String symbol) {
        Ticker ticker = lastTickers.get(symbol);
        if (ticker == null) {
            ticker = new Ticker(symbol);
            lastTickers.put(symbol, ticker);
        }
        return ticker;
    }

    /**
     * set last ticker to local cache
     */
    @Override
    public void setLastTicker(String symbol, Ticker ticker) {
        lastTickers.put(symbol, ticker);
    }

    /**
     * get last kline periods by the symbol
     */
    @Override
    public Map<KlinePeriod, Kline> getLastKlinePeriods(String symbol) {
        Map<KlinePeriod, Kline> klinePeriods = lastKlineCache.get(symbol);
        if (klinePeriods == null) {
            klinePeriods = new HashMap<>();
            lastKlineCache.put(symbol, klinePeriods);
        }
        return klinePeriods;
    }

    /**
     * get orderClientIds
     */
    @Override
    public ConcurrentHashSet<String> getOrderClientIds() {
        return orderClientIds;
    }

    /**
     * get positionClientIds
     */
    @Override
    public ConcurrentHashSet<String> getPositionClientIds() {
        return positionClientIds;
    }

    /**
     * get debtClientIds
     */
    @Override
    public ConcurrentHashSet<String> getDebtClientIds() {
        return debtClientIds;
    }

    /**
     * get stopOrderCache by the symbol
     */
    @Override
    public StopOrderCache getStopOrderCache(String symbol) {
        StopOrderCache stopOrderCache = stopOrderCaches.get(symbol);
        if (stopOrderCache == null) {
            stopOrderCache = new StopOrderCache();
            stopOrderCaches.put(symbol, stopOrderCache);
        }
        return stopOrderCache;
    }

    /**
     * get system parameter of type long
     */
    private Long getSystemParameterOfLong(SystemParameter parameter) {
        String value = systemParameters.get(parameter);
        return value == null ? 0L : Long.parseLong(value);
    }

    /**
     * get system parameter of type boolean
     */
    private Boolean getSystemParameterOfBoolean(SystemParameter parameter) {
        String value = systemParameters.get(parameter);
        return value == null ? false : Boolean.valueOf(value);
    }

    /**
     * init orderClientIds
     */
    private void initOrderClientIds() {
        Set<String> clientIds = redisService.getOrderClientIds();
        if (!CollectionUtils.isEmpty(clientIds)) {
            clientIds.forEach(clientId -> orderClientIds.add(clientId));
        }
    }

    /**
     * init positionClientIds
     */
    private void initPositionClientIds() {
        Set<String> clientIds = redisService.getPositionClientIds();
        if (!CollectionUtils.isEmpty(clientIds)) {
            clientIds.forEach(clientId -> positionClientIds.add(clientId));
        }
    }

    /**
     * init debtClientIds
     */
    private void initDebtClientIds() {
        Set<String> clientIds = redisService.getDebtClientIds();
        if (!CollectionUtils.isEmpty(clientIds)) {
            clientIds.forEach(clientId -> debtClientIds.add(clientId));
        }
    }

    /**
     * Load all clients with open positions into local cache
     */
    private void loadAllClientWithPosition() {
        positionClientIds.values().parallelStream().forEach(clientId -> getClientEntity(clientId));
    }

    /**
     * find client entity by the clientId
     */
    private ClientEntity findClientEntity(String clientId) {
        ClientEntity client = ClientEntity.newInstance(false);
        client.setClientId(clientId);
        client.initBalances(redisService.getBalancesByClientId(clientId));
        client.initPositions(redisService.getPositionsByClientId(clientId));
        client.initOrders(redisService.getOrdersByClientId(clientId));
        client.initSettings(redisService.getSettingsByClientId(clientId));

        /**
         * fee rate
         */
        FeeRate feeRate = redisService.getFeeRateByClientId(clientId);
        if (feeRate == null) {
            // If there is no fee rate for the client in redis, the default fee rate is used.
            // Using constructor is more efficient than using builder.
            feeRate = new FeeRate(clientId, new BigDecimal(getSystemParameter(SystemParameter.DEFAULT_FEE_RATE_MAKER)),
                new BigDecimal(getSystemParameter(SystemParameter.DEFAULT_FEE_RATE_TAKER)));
        }
        client.setFeeRate(feeRate);

        /**
         * trade authority
         */
        Integer tradeAuthority = redisService.getTradeAuthorityByClientId(clientId);
        if (tradeAuthority == null) {
            tradeAuthority = Constants.DEFAULT_TRADE_AUTHORITY;
        }
        client.setTradeAuthority(tradeAuthority);

        /**
         * last trade id is not saved to redis, default value is ZERO.
         */
        client.setLastTradeId(0L);

        client.refreshUpdateTime();

        return client;
    }

    /**
     * find client collateral by the clientId
     */
    private BigDecimal findClientCollateral(String clientId) {
        BigDecimal collateral = BigDecimal.ZERO;
        String clearAsset = getSystemParameter(SystemParameter.CLEAR_ASSET);
        for (AssetBalance balance : getClientEntity(clientId).getBalances().values()) {
            if (!balance.getAsset().equals(clearAsset) && balance.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                AssetInfo assetInfo = getAssetInfo(balance.getAsset());
                if (assetInfo == null) {
                    throw new TradeException(TradeError.ASSET_NOT_EXIST);
                }
                BigDecimal indexPrice = getSymbolIndicator(assetInfo.getIndexPriceSymbol()).getIndexPrice();
                if (indexPrice == null) {
                    throw new TradeException(TradeError.INDEX_PRICE_NOT_EXIST);
                }
                collateral = collateral.add(balance.getBalance().multiply(indexPrice)
                    .multiply(BigDecimal.ONE.subtract(assetInfo.getDiscount())));
            }
        }
        return collateral;
    }

}
