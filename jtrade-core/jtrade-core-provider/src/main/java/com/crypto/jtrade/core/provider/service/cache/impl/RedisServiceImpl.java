package com.crypto.jtrade.core.provider.service.cache.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.AssetInfo;
import com.crypto.jtrade.common.model.ClientSetting;
import com.crypto.jtrade.common.model.FeeRate;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.util.Utils;
import com.crypto.jtrade.core.provider.model.landing.RedisOperation;
import com.crypto.jtrade.core.provider.service.cache.RedisService;

import lombok.extern.slf4j.Slf4j;

/**
 * Redis service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class RedisServiceImpl implements RedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * get all system parameter from redis
     */
    @Override
    public Map<SystemParameter, String> getSystemParameters() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(Constants.REDIS_KEY_SYSTEM_PARAMETER);
        return entries.entrySet().stream().collect(Collectors
            .toMap(entry -> SystemParameter.valueOf((String)entry.getKey()), entry -> (String)entry.getValue()));
    }

    /**
     * get all symbols from redis
     */
    @Override
    public Map<String, SymbolInfo> getSymbols() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(Constants.REDIS_KEY_SYMBOL);
        return entries.entrySet().stream().collect(
            Collectors.toMap(entry -> (String)entry.getKey(), entry -> SymbolInfo.toObject((String)entry.getValue())));
    }

    /**
     * get all assets from redis
     */
    @Override
    public Map<String, AssetInfo> getAssets() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(Constants.REDIS_KEY_ASSET);
        return entries.entrySet().stream().collect(
            Collectors.toMap(entry -> (String)entry.getKey(), entry -> AssetInfo.toObject((String)entry.getValue())));
    }

    /**
     * get asset balance by the clientId from redis
     */
    @Override
    public ConcurrentMap<String, AssetBalance> getBalancesByClientId(String clientId) {
        String key = Utils.format(Constants.REDIS_KEY_BALANCE, clientId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        return entries.entrySet().stream().collect(Collectors.toConcurrentMap(entry -> (String)entry.getKey(),
            entry -> AssetBalance.toObject((String)entry.getValue())));
    }

    /**
     * get position by the clientId from redis
     */
    @Override
    public ConcurrentMap<String, Position> getPositionsByClientId(String clientId) {
        String key = Utils.format(Constants.REDIS_KEY_POSITION, clientId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        return entries.entrySet().stream().collect(Collectors.toConcurrentMap(entry -> (String)entry.getKey(),
            entry -> Position.toObject((String)entry.getValue())));
    }

    /**
     * get order by the clientId from redis
     */
    @Override
    public ConcurrentMap<Long, Order> getOrdersByClientId(String clientId) {
        String key = Utils.format(Constants.REDIS_KEY_ORDER, clientId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        return entries.entrySet().stream().collect(Collectors.toConcurrentMap(
            entry -> Long.parseLong((String)entry.getKey()), entry -> Order.toObject((String)entry.getValue())));
    }

    /**
     * get setting of the clientId from redis
     */
    @Override
    public ConcurrentMap<String, ClientSetting> getSettingsByClientId(String clientId) {
        String key = Utils.format(Constants.REDIS_KEY_CLIENT_SETTING, clientId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        return entries.entrySet().stream().collect(Collectors.toConcurrentMap(entry -> (String)entry.getKey(),
            entry -> ClientSetting.toObject((String)entry.getValue())));
    }

    /**
     * get fee rate of the clientId from redis
     */
    @Override
    public FeeRate getFeeRateByClientId(String clientId) {
        String key = Utils.format(Constants.REDIS_KEY_CLIENT_FEE_RATE, clientId);
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? null : FeeRate.toObject(value);
    }

    /**
     * get trade authority of the clientId from redis
     */
    @Override
    public Integer getTradeAuthorityByClientId(String clientId) {
        String key = Utils.format(Constants.REDIS_KEY_CLIENT_AUTHORITY, clientId);
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? null : Integer.parseInt(value);
    }

    /**
     * Batch write to redis
     */
    @Override
    public void batchWriteOperations(final List<RedisOperation> operationList, final boolean transactionSupport) {
        redisTemplate.executePipelined(new SessionCallback<Object>() {

            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                if (transactionSupport)
                    operations.multi();
                for (RedisOperation op : operationList) {
                    switch (op.getRedisOp()) {
                        case VALUE:
                            if (op.getDeleted()) {
                                operations.delete(op.getRedisKey());
                            } else {
                                operations.opsForValue().set(op.getRedisKey(), op.getValue());
                            }
                            break;
                        case HASH:
                            if (op.getDeleted()) {
                                operations.opsForHash().delete(op.getRedisKey(), op.getHashKey());
                            } else {
                                operations.opsForHash().put(op.getRedisKey(), op.getHashKey(), op.getValue());
                            }
                            break;
                        case SET:
                            if (op.getDeleted()) {
                                operations.opsForSet().remove(op.getRedisKey(), op.getValue());
                            } else {
                                operations.opsForSet().add(op.getRedisKey(), op.getValue());
                            }
                            break;
                        case LIST:
                            if (op.getDeleted()) {
                                operations.delete(op.getRedisKey());
                            } else {
                                operations.opsForList().rightPush(op.getRedisKey(), op.getValue());
                            }
                            break;
                        default:
                            break;
                    }
                }
                if (transactionSupport)
                    operations.exec();
                return null;
            }
        });
    }

    /**
     * get all open orders
     */
    @Override
    public List<Object> getAllOpenOrders() {
        Set<String> clientIds = redisTemplate.opsForSet().members(Constants.REDIS_KEY_ORDER_CLIENTS);
        if (CollectionUtils.isEmpty(clientIds)) {
            return new ArrayList<>();
        } else {
            List<Object> orderList = redisTemplate.executePipelined(new SessionCallback<Object>() {

                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    clientIds.forEach(clientId -> {
                        String key = Utils.format(Constants.REDIS_KEY_ORDER, clientId);
                        operations.opsForHash().entries(key);
                    });
                    return null;
                }
            });
            return orderList;
        }
    }

    /**
     * get orderClientIds
     */
    @Override
    public Set<String> getOrderClientIds() {
        return redisTemplate.opsForSet().members(Constants.REDIS_KEY_ORDER_CLIENTS);
    }

    /**
     * get positionClientIds
     */
    @Override
    public Set<String> getPositionClientIds() {
        return redisTemplate.opsForSet().members(Constants.REDIS_KEY_POSITION_CLIENTS);
    }

    @Override
    public Set<String> getDebtClientIds() {
        return redisTemplate.opsForSet().members(Constants.REDIS_KEY_DEBT_CLIENTS);
    }

    /**
     * get premiums by the symbol
     */
    @Override
    public List<String> getPremiumsBySymbol(String symbol) {
        String key = Utils.format(Constants.REDIS_KEY_PREMIUM, symbol);
        Long size = redisTemplate.opsForList().size(key);
        if (size > 0) {
            return redisTemplate.opsForList().range(key, 0, size - 1);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * get markPrices by symbol
     */
    @Override
    public Map<Object, Object> getMarkPricesBySymbol(String redisKey) {
        return redisTemplate.opsForHash().entries(redisKey);
    }

    /**
     * batch delete by prefix
     */
    @Override
    public void deleteByPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix);
        if (!CollectionUtils.isEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

}
