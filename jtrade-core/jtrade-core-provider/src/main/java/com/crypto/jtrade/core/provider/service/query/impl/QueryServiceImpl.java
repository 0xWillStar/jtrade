package com.crypto.jtrade.core.provider.service.query.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.KlinePeriod;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.Depth;
import com.crypto.jtrade.common.model.Kline;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.SymbolIndicator;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.model.Ticker;
import com.crypto.jtrade.core.provider.model.convert.BeanMapping;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.query.QueryService;
import com.crypto.jtrade.core.provider.util.ClientLockHelper;

import lombok.extern.slf4j.Slf4j;

/**
 * Query service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class QueryServiceImpl implements QueryService {

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private BeanMapping beanMapping;

    /**
     * get symbols
     */
    @Override
    public List<SymbolInfo> getSymbols() {
        return new ArrayList<>(localCache.getAllSymbols().values());
    }

    @Override
    public SymbolIndicator getSymbolIndicator(String symbol) {
        return localCache.getSymbolIndicator(symbol);
    }

    /**
     * get last depth by the symbol
     */
    @Override
    public Depth getDepth(String symbol) {
        return localCache.getLastDepth(symbol);
    }

    /**
     * get last ticker by the symbol
     */
    @Override
    public Ticker getTicker(String symbol) {
        Lock lock = localCache.getMarketLock(symbol);
        lock.lock();
        try {
            Ticker ticker = localCache.getLastTicker(symbol);
            return beanMapping.clone(ticker);
        } finally {
            lock.unlock();
        }
    }

    /**
     * get last kline by the symbol
     */
    @Override
    public Kline getKline(String symbol, String period) {
        Lock lock = localCache.getMarketLock(symbol);
        lock.lock();
        try {
            Kline kline = localCache.getLastKlinePeriods(symbol).get(KlinePeriod.fromPeriod(period));
            return beanMapping.clone(kline);
        } finally {
            lock.unlock();
        }
    }

    /**
     * get open orders of the client
     */
    @Override
    public List<Order> getOpenOrders(String clientId) {
        Lock lock = ClientLockHelper.getLock(clientId);
        lock.lock();
        try {
            Collection<Order> orderCollection = localCache.getClientEntity(clientId).getOrders().values();
            List<Order> orderList = new ArrayList<>(orderCollection.size());
            for (Order order : orderCollection) {
                orderList.add(beanMapping.clone(order));
            }
            return orderList;
        } finally {
            lock.unlock();
        }
    }

    /**
     * get positions of the client
     */
    @Override
    public List<Position> getPositions(String clientId) {
        Lock lock = ClientLockHelper.getLock(clientId);
        lock.lock();
        try {
            Collection<Position> positionCollection = localCache.getClientEntity(clientId).getPositions().values();
            List<Position> positionList = new ArrayList<>(positionCollection.size());
            for (Position position : positionCollection) {
                positionList.add(beanMapping.clone(position));
            }
            return positionList;
        } finally {
            lock.unlock();
        }
    }

    /**
     * get asset balances of the client
     */
    @Override
    public List<AssetBalance> getBalances(String clientId) {
        Lock lock = ClientLockHelper.getLock(clientId);
        lock.lock();
        try {
            Collection<AssetBalance> balanceCollection = localCache.getClientEntity(clientId).getBalances().values();
            List<AssetBalance> balanceList = new ArrayList<>(balanceCollection.size());
            for (AssetBalance balance : balanceCollection) {
                balanceList.add(beanMapping.clone(balance));
            }
            return balanceList;
        } finally {
            lock.unlock();
        }
    }
}
