package com.crypto.jtrade.core.provider.service.cache;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.AssetInfo;
import com.crypto.jtrade.common.model.ClientSetting;
import com.crypto.jtrade.common.model.FeeRate;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.util.OnlyForTest;
import com.crypto.jtrade.core.provider.model.landing.RedisOperation;

/**
 * Redis service
 *
 * @author 0xWill
 **/
public interface RedisService {

    /**
     * get all system parameter from redis
     */
    Map<SystemParameter, String> getSystemParameters();

    /**
     * get all symbols from redis
     */
    Map<String, SymbolInfo> getSymbols();

    /**
     * get all assets from redis
     */
    Map<String, AssetInfo> getAssets();

    /**
     * get asset balance by the clientId from redis
     */
    ConcurrentMap<String, AssetBalance> getBalancesByClientId(String clientId);

    /**
     * get position by the clientId from redis
     */
    ConcurrentMap<String, Position> getPositionsByClientId(String clientId);

    /**
     * get order by the clientId from redis
     */
    ConcurrentMap<Long, Order> getOrdersByClientId(String clientId);

    /**
     * get setting of the clientId from redis
     */
    ConcurrentMap<String, ClientSetting> getSettingsByClientId(String clientId);

    /**
     * get fee rate of the clientId from redis
     */
    FeeRate getFeeRateByClientId(String clientId);

    /**
     * get trade authority of the clientId from redis
     */
    Integer getTradeAuthorityByClientId(String clientId);

    /**
     * Batch write to redis
     */
    void batchWriteOperations(List<RedisOperation> operationList, boolean transactionSupport);

    /**
     * get all open orders
     */
    List<Object> getAllOpenOrders();

    /**
     * get orderClientIds
     */
    Set<String> getOrderClientIds();

    /**
     * get positionClientIds
     */
    Set<String> getPositionClientIds();

    /**
     * get debtClientIds
     */
    Set<String> getDebtClientIds();

    /**
     * get premiums by symbol
     */
    List<String> getPremiumsBySymbol(String symbol);

    /**
     * get markPrices by symbol
     */
    Map<Object, Object> getMarkPricesBySymbol(String redisKey);

    /**
     * batch delete by prefix
     */
    @OnlyForTest
    void deleteByPrefix(String prefix);

}
