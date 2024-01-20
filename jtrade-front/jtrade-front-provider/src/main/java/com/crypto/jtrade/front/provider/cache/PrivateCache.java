package com.crypto.jtrade.front.provider.cache;

import java.util.List;
import java.util.Map;

import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.Bill;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.Trade;

public interface PrivateCache {

    /**
     * get all asset balance by the clientId
     */
    Map<String, AssetBalance> getBalances(String clientId);

    /**
     * set asset balance
     */
    void setBalance(AssetBalance balance);

    /**
     * get all position by the clientId
     */
    Map<String, Position> getPositions(String clientId);

    /**
     * set position
     */
    void setPosition(Position position);

    /**
     * get open orders by the clientId
     */
    Map<Long, Order> getOpenOrders(String clientId);

    /**
     * get finish orders by the clientId
     */
    List<Order> getFinishOrders(String clientId);

    /**
     * set order
     */
    void setOrder(Order order);

    /**
     * get trades by the clientId
     */
    List<Trade> getTrades(String clientId);

    /**
     * set trade
     */
    void setTrade(Trade trade);

    /**
     * get bills by the clientId
     */
    List<Bill> getBills(String clientId);

    /**
     * set bill
     */
    void setBill(Bill bill);

}
