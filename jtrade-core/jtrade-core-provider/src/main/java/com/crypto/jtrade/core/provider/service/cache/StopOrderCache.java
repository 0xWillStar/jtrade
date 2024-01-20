package com.crypto.jtrade.core.provider.service.cache;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import com.crypto.jtrade.common.constants.OrderSide;
import com.crypto.jtrade.common.constants.OrderType;
import com.crypto.jtrade.common.constants.WorkingType;
import com.crypto.jtrade.common.model.Order;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * stop order cache
 *
 * @author 0xWill
 **/
@Data
public class StopOrderCache {

    private ReentrantLock lock = new ReentrantLock();

    private TreeMap<AscendKey, Order> lastAscOrders = new TreeMap<>();

    private TreeMap<DescendKey, Order> lastDescOrders = new TreeMap<>();

    private TreeMap<AscendKey, Order> markAscOrders = new TreeMap<>();

    private TreeMap<DescendKey, Order> markDescOrders = new TreeMap<>();

    /**
     * add new order to cache
     */
    public void addOrder(Order order) {
        lock.lock();
        try {
            if (order.getWorkingType() == WorkingType.LAST_PRICE) {
                addOrder(lastAscOrders, lastDescOrders, order);
            } else if (order.getWorkingType() == WorkingType.MARK_PRICE) {
                addOrder(markAscOrders, markDescOrders, order);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * remove order from cache
     */
    public void removeOrder(Order order) {
        lock.lock();
        try {
            if (order.getWorkingType() == WorkingType.LAST_PRICE) {
                removeOrder(lastAscOrders, lastDescOrders, order);
            } else if (order.getWorkingType() == WorkingType.MARK_PRICE) {
                removeOrder(markAscOrders, markDescOrders, order);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * get trigger orders
     */
    public List<Order> getTriggerOrders(WorkingType workingType, BigDecimal triggerPrice) {
        lock.lock();
        try {
            List<Order> orderList = null;
            if (workingType == WorkingType.LAST_PRICE) {
                orderList = getTriggerOrders(lastAscOrders, lastDescOrders, triggerPrice);
            } else if (workingType == WorkingType.MARK_PRICE) {
                orderList = getTriggerOrders(markAscOrders, markDescOrders, triggerPrice);
            }
            return orderList;
        } finally {
            lock.unlock();
        }
    }

    /**
     * add new order to cache
     */
    private void addOrder(TreeMap<AscendKey, Order> ascOrders, TreeMap<DescendKey, Order> descOrders, Order order) {
        if (order.getType() == OrderType.STOP || order.getType() == OrderType.STOP_MARKET) {
            /**
             * BUY: latest price ("MARK_PRICE" or "CONTRACT_PRICE") >= stopPrice
             * 
             * SELL: latest price ("MARK_PRICE" or "CONTRACT_PRICE") <= stopPrice
             */
            if (order.getSide() == OrderSide.BUY) {
                ascOrders.put(new AscendKey(order.getStopPrice(), order.getOrderId()), order);
            } else {
                descOrders.put(new DescendKey(order.getStopPrice(), order.getOrderId()), order);
            }
        } else if (order.getType() == OrderType.TAKE_PROFIT || order.getType() == OrderType.TAKE_PROFIT_MARKET) {
            /**
             * BUY: latest price ("MARK_PRICE" or "CONTRACT_PRICE") <= stopPrice
             * 
             * SELL: latest price ("MARK_PRICE" or "CONTRACT_PRICE") >= stopPrice
             */
            if (order.getSide() == OrderSide.BUY) {
                descOrders.put(new DescendKey(order.getStopPrice(), order.getOrderId()), order);
            } else {
                ascOrders.put(new AscendKey(order.getStopPrice(), order.getOrderId()), order);
            }
        }
    }

    /**
     * remove order from cache
     */
    private void removeOrder(TreeMap<AscendKey, Order> ascOrders, TreeMap<DescendKey, Order> descOrders, Order order) {
        if (order.getType() == OrderType.STOP || order.getType() == OrderType.STOP_MARKET) {
            if (order.getSide() == OrderSide.BUY) {
                ascOrders.remove(new AscendKey(order.getStopPrice(), order.getOrderId()));
            } else {
                descOrders.remove(new DescendKey(order.getStopPrice(), order.getOrderId()));
            }
        } else if (order.getType() == OrderType.TAKE_PROFIT || order.getType() == OrderType.TAKE_PROFIT_MARKET) {
            if (order.getSide() == OrderSide.BUY) {
                descOrders.remove(new DescendKey(order.getStopPrice(), order.getOrderId()));
            } else {
                ascOrders.remove(new AscendKey(order.getStopPrice(), order.getOrderId()));
            }
        }
    }

    /**
     * get trigger orders
     */
    private List<Order> getTriggerOrders(TreeMap<AscendKey, Order> ascOrders, TreeMap<DescendKey, Order> descOrders,
        BigDecimal triggerPrice) {
        List<Order> orderList = new ArrayList<>();
        /**
         * Ascend orders
         */
        Iterator<Order> ascIterator = ascOrders.values().iterator();
        while (ascIterator.hasNext()) {
            Order ascOrder = ascIterator.next();
            if (triggerPrice.compareTo(ascOrder.getStopPrice()) >= 0) {
                orderList.add(ascOrder);
                ascIterator.remove();
            } else {
                break;
            }
        }
        /**
         * Descend orders
         */
        Iterator<Order> descIterator = descOrders.values().iterator();
        while (descIterator.hasNext()) {
            Order desOrder = descIterator.next();
            if (triggerPrice.compareTo(desOrder.getStopPrice()) <= 0) {
                orderList.add(desOrder);
                descIterator.remove();
            } else {
                break;
            }
        }
        return orderList;
    }

    @Data
    @AllArgsConstructor
    private static class AscendKey implements Comparable<AscendKey> {

        private BigDecimal stopPrice;

        private Long orderId;

        @Override
        public int compareTo(AscendKey order) {
            return this.stopPrice.compareTo(order.getStopPrice());
        }
    }

    @Data
    @AllArgsConstructor
    private static class DescendKey implements Comparable<DescendKey> {

        private BigDecimal stopPrice;

        private Long orderId;

        @Override
        public int compareTo(DescendKey order) {
            return order.getStopPrice().compareTo(this.stopPrice);
        }
    }

}
