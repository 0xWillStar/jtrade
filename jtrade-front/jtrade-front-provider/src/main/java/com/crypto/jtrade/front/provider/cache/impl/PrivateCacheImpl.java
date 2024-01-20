package com.crypto.jtrade.front.provider.cache.impl;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.crypto.jtrade.common.constants.OrderStatus;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.model.Bill;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.model.Trade;
import com.crypto.jtrade.common.util.Utils;
import com.crypto.jtrade.core.api.QueryApi;
import com.crypto.jtrade.front.provider.cache.PrivateCache;
import com.crypto.jtrade.front.provider.cache.PublicCache;
import com.crypto.jtrade.front.provider.config.FrontConfig;
import com.crypto.jtrade.front.provider.mapper.BillMapper;
import com.crypto.jtrade.front.provider.mapper.OrderMapper;
import com.crypto.jtrade.front.provider.mapper.TradeMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import lombok.extern.slf4j.Slf4j;

/**
 * private data cache
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class PrivateCacheImpl implements PrivateCache {

    private static final Duration CN_CACHE_EXPIRE_DURATION = Duration.ofHours(1);

    /**
     * AssetBalance cache, KEY1: clientId, KEY2: asset
     */
    private LoadingCache<String, Map<String, AssetBalance>> balanceCache =
        Caffeine.newBuilder().expireAfterAccess(CN_CACHE_EXPIRE_DURATION).build(key -> findBalances(key));

    /**
     * Position cache, KEY1: clientId, KEY2: symbol_positionSide, now only symbol
     */
    private LoadingCache<String, Map<String, Position>> positionCache =
        Caffeine.newBuilder().expireAfterAccess(CN_CACHE_EXPIRE_DURATION).build(key -> findPositions(key));

    /**
     * OpenOrder cache, KEY1: clientId, KEY2: orderId
     */
    private LoadingCache<String, Map<Long, Order>> openOrderCache =
        Caffeine.newBuilder().expireAfterAccess(CN_CACHE_EXPIRE_DURATION).build(key -> findOpenOrders(key));

    /**
     * FinishOrder cache, KEY: clientId
     */
    private LoadingCache<String, CircularFifoQueue<Order>> finishOrderCache =
        Caffeine.newBuilder().expireAfterAccess(CN_CACHE_EXPIRE_DURATION).build(key -> findFinishOrders(key));

    /**
     * Trade cache, KEY: clientId
     */
    private LoadingCache<String, CircularFifoQueue<Trade>> tradeCache =
        Caffeine.newBuilder().expireAfterAccess(CN_CACHE_EXPIRE_DURATION).build(key -> findTrades(key));

    /**
     * Bill cache, KEY: clientId
     */
    private LoadingCache<String, CircularFifoQueue<Bill>> billCache =
        Caffeine.newBuilder().expireAfterAccess(CN_CACHE_EXPIRE_DURATION).build(key -> findBills(key));

    @Autowired
    private FrontConfig frontConfig;

    @Autowired
    private PublicCache publicCache;

    @Autowired
    private QueryApi queryApi;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TradeMapper tradeMapper;

    @Autowired
    private BillMapper billMapper;

    /**
     * get all asset balance by the clientId
     */
    @Override
    public Map<String, AssetBalance> getBalances(String clientId) {
        return balanceCache.get(clientId);
    }

    /**
     * set asset balance
     */
    @Override
    public void setBalance(AssetBalance balance) {
        Map<String, AssetBalance> balanceMap = balanceCache.getIfPresent(balance.getClientId());
        if (balanceMap != null) {
            balanceMap.put(balance.getAsset(), balance);
        }
    }

    /**
     * get all position by the clientId
     */
    @Override
    public Map<String, Position> getPositions(String clientId) {
        return positionCache.get(clientId);
    }

    /**
     * set position
     */
    @Override
    public void setPosition(Position position) {
        Map<String, Position> positionMap = positionCache.getIfPresent(position.getClientId());
        if (positionMap != null) {
            BigDecimal total =
                position.getPositionAmt().abs().add(position.getShortFrozenAmt().add(position.getLongFrozenAmt()));
            if (total.compareTo(BigDecimal.ZERO) == 0) {
                positionMap.remove(position.getSymbol());
            } else {
                positionMap.put(position.getSymbol(), position);
            }
        }
    }

    /**
     * get open orders by the clientId
     */
    @Override
    public Map<Long, Order> getOpenOrders(String clientId) {
        return openOrderCache.get(clientId);
    }

    /**
     * get finish orders by the clientId
     */
    @Override
    public List<Order> getFinishOrders(String clientId) {
        CircularFifoQueue<Order> finishOrders = finishOrderCache.get(clientId);
        return finishOrders == null ? new ArrayList<>() : new ArrayList<>(finishOrders);
    }

    /**
     * set order
     */
    @Override
    public void setOrder(Order order) {
        // openOrders
        Map<Long, Order> openOrders = openOrderCache.getIfPresent(order.getClientId());
        if (openOrders != null) {
            if (order.getStatus() == OrderStatus.CANCELED || order.getStatus() == OrderStatus.FILLED
                || order.getStatus() == OrderStatus.EXPIRED || order.getStatus() == OrderStatus.REJECTED) {
                openOrders.remove(order.getOrderId());
            } else {
                openOrders.put(order.getOrderId(), order);
            }
        }
        // finishOrders
        CircularFifoQueue<Order> finishOrders = finishOrderCache.getIfPresent(order.getClientId());
        if (finishOrders != null) {
            if (order.getStatus() == OrderStatus.CANCELED || order.getStatus() == OrderStatus.FILLED
                || order.getStatus() == OrderStatus.EXPIRED || order.getStatus() == OrderStatus.REJECTED) {
                finishOrders.offer(order);
            }
        }
    }

    /**
     * get trades by the clientId
     */
    @Override
    public List<Trade> getTrades(String clientId) {
        CircularFifoQueue<Trade> trades = tradeCache.get(clientId);
        return trades == null ? new ArrayList<>() : new ArrayList<>(trades);
    }

    /**
     * set trade
     */
    @Override
    public void setTrade(Trade trade) {
        CircularFifoQueue<Trade> trades = tradeCache.getIfPresent(trade.getClientId());
        if (trades != null) {
            trades.offer(trade);
        }
    }

    /**
     * get bills by the clientId
     */
    @Override
    public List<Bill> getBills(String clientId) {
        CircularFifoQueue<Bill> bills = billCache.get(clientId);
        return bills == null ? new ArrayList<>() : new ArrayList<>(bills);
    }

    /**
     * set bill
     */
    @Override
    public void setBill(Bill bill) {
        CircularFifoQueue<Bill> bills = billCache.getIfPresent(bill.getClientId());
        if (bills != null) {
            bills.offer(bill);
        }
    }

    /**
     * find asset balance by client id
     */
    private Map<String, AssetBalance> findBalances(String clientId) {
        BaseResponse<List<AssetBalance>> response = queryApi.getBalances(clientId);
        if (!response.isError()) {
            List<AssetBalance> balanceList = response.getData();
            return balanceList.stream().collect(Collectors.toConcurrentMap(AssetBalance::getAsset, balance -> balance));
        } else {
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * find positions by client id
     */
    private Map<String, Position> findPositions(String clientId) {
        BaseResponse<List<Position>> response = queryApi.getPositions(clientId);
        if (!response.isError()) {
            List<Position> positionList = response.getData();
            return positionList.stream().collect(Collectors.toConcurrentMap(Position::getSymbol, position -> position));
        } else {
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * find open orders by client id
     */
    private Map<Long, Order> findOpenOrders(String clientId) {
        BaseResponse<List<Order>> response = queryApi.getOpenOrders(clientId);
        if (!response.isError()) {
            List<Order> orderList = response.getData();
            return orderList.stream().collect(Collectors.toConcurrentMap(Order::getOrderId, order -> order));
        } else {
            return new ConcurrentHashMap<>();
        }
    }

    /**
     * find finish order by client id
     */
    private CircularFifoQueue<Order> findFinishOrders(String clientId) {
        CircularFifoQueue<Order> orderQueue = new CircularFifoQueue<>(frontConfig.getClientHistoryDefaultSize());
        long startTime = System.currentTimeMillis() - frontConfig.getClientHistoryDefaultDays() * 24 * 3600 * 1000;
        List<Order> orderList =
            orderMapper.getFinishOrderList(clientId, startTime, frontConfig.getClientHistoryDefaultSize());
        if (!CollectionUtils.isEmpty(orderList)) {
            for (Order order : orderList) {
                SymbolInfo symbolInfo = publicCache.getSymbolInfo(order.getSymbol());
                Utils.formatOrder(order, symbolInfo);
                orderQueue.offer(order);
            }
        }
        return orderQueue;
    }

    /**
     * find trade by client id
     */
    private CircularFifoQueue<Trade> findTrades(String clientId) {
        CircularFifoQueue<Trade> tradeQueue = new CircularFifoQueue<>(frontConfig.getClientHistoryDefaultSize());
        long startTime = System.currentTimeMillis() - frontConfig.getClientHistoryDefaultDays() * 24 * 3600 * 1000;
        List<Trade> tradeList =
            tradeMapper.getTradeListByClient(clientId, startTime, frontConfig.getClientHistoryDefaultSize());
        if (!CollectionUtils.isEmpty(tradeList)) {
            for (Trade trade : tradeList) {
                SymbolInfo symbolInfo = publicCache.getSymbolInfo(trade.getSymbol());
                Utils.formatTrade(trade, symbolInfo);
                tradeQueue.offer(trade);
            }
        }
        return tradeQueue;
    }

    /**
     * find bill by client id
     */
    private CircularFifoQueue<Bill> findBills(String clientId) {
        CircularFifoQueue<Bill> billQueue = new CircularFifoQueue<>(frontConfig.getClientHistoryDefaultSize());
        long startTime = System.currentTimeMillis() - frontConfig.getClientHistoryDefaultDays() * 24 * 3600 * 1000;
        List<Bill> billList = billMapper.getBillList(clientId, startTime, frontConfig.getClientHistoryDefaultSize());
        if (!CollectionUtils.isEmpty(billList)) {
            for (Bill bill : billList) {
                Utils.formatBill(bill);
                billQueue.offer(bill);
            }
        }
        return billQueue;
    }

}
