package com.crypto.jtrade.core.provider.service.cache;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.crypto.jtrade.common.constants.KlinePeriod;
import com.crypto.jtrade.common.constants.ProductType;
import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.model.AssetInfo;
import com.crypto.jtrade.common.model.Depth;
import com.crypto.jtrade.common.model.Kline;
import com.crypto.jtrade.common.model.SymbolIndicator;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.model.Ticker;
import com.crypto.jtrade.common.util.concurrent.ConcurrentHashSet;

/**
 * Local cache service
 *
 * @author 0xWill
 **/
public interface LocalCacheService {

    /**
     * get product type
     */
    ProductType getProductType();

    /**
     * get a system parameter from local cache
     */
    String getSystemParameter(SystemParameter parameter);

    /**
     * set a system parameter into local cache
     */
    void setSystemParameter(SystemParameter parameter, String value);

    /**
     * get a symbol info instance from local cache
     */
    SymbolInfo getSymbolInfo(String symbol);

    /**
     * get symbol write lock
     */
    ReentrantLock getSymbolWriteLock();

    /**
     * get all symbols from local cache
     */
    Map<String, SymbolInfo> getAllSymbols();

    /**
     * set a symbol info into local cache
     */
    void setSymbolInfo(SymbolInfo symbol);

    /**
     * get asset info from local cache
     */
    AssetInfo getAssetInfo(String asset);

    /**
     * get all assets from local cache
     */
    Map<String, AssetInfo> getAllAssets();

    /**
     * set asset info into local cache
     */
    void setAssetInfo(AssetInfo assetInfo);

    /**
     * get a symbol indicator from local cache
     */
    SymbolIndicator getSymbolIndicator(String symbol);

    /**
     * get all symbol indicator
     */
    Map<String, SymbolIndicator> getSymbolIndicators();

    /**
     * get a client entity from local cache
     */
    ClientEntity getClientEntity(String clientId);

    /**
     * get client collateral from local cache
     */
    BigDecimal getClientCollateral(String clientId);

    /**
     * invalidate client collateral cache
     */
    void invalidateClientCollateral(String clientId);

    /**
     * Tests if the specified object is a key in the marketLocks.
     */
    boolean containsMarketLock(String symbol);

    /**
     * get market lock by the symbol
     */
    ReentrantLock getMarketLock(String symbol);

    /**
     * set market lock to the marketLocks
     */
    void setMarketLock(String symbol, ReentrantLock lock);

    /**
     * get last depth by the symbol
     */
    Depth getLastDepth(String symbol);

    /**
     * set last depth to local cache
     */
    void setLastDepth(String symbol, Depth depth);

    /**
     * get last ticker by the symbol
     */
    Ticker getLastTicker(String symbol);

    /**
     * set last ticker to local cache
     */
    void setLastTicker(String symbol, Ticker ticker);

    /**
     * get last kline periods by the symbol
     */
    Map<KlinePeriod, Kline> getLastKlinePeriods(String symbol);

    /**
     * get orderClientIds
     */
    ConcurrentHashSet<String> getOrderClientIds();

    /**
     * get positionClientIds
     */
    ConcurrentHashSet<String> getPositionClientIds();

    /**
     * get debtClientIds
     */
    ConcurrentHashSet<String> getDebtClientIds();

    /**
     * get stopOrderCache by the symbol
     */
    StopOrderCache getStopOrderCache(String symbol);

}
