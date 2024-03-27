package com.crypto.jtrade.core.provider.service.match.impl;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.Lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.*;
import com.crypto.jtrade.common.model.*;
import com.crypto.jtrade.core.provider.config.CoreConfig;
import com.crypto.jtrade.core.provider.model.convert.BeanMapping;
import com.crypto.jtrade.core.provider.model.landing.MatchedLandings;
import com.crypto.jtrade.core.provider.model.landing.OrderMatchedLanding;
import com.crypto.jtrade.core.provider.model.session.TradeSession;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.landing.MySqlLanding;
import com.crypto.jtrade.core.provider.service.landing.RedisLanding;
import com.crypto.jtrade.core.provider.service.match.SymbolMatcher;
import com.crypto.jtrade.core.provider.service.publish.PrivatePublish;
import com.crypto.jtrade.core.provider.service.publish.PublicPublish;
import com.crypto.jtrade.core.provider.service.rule.TradeRule;
import com.crypto.jtrade.core.provider.service.rule.TradeRuleManager;
import com.crypto.jtrade.core.provider.util.ClientLockHelper;
import com.crypto.jtrade.core.provider.util.SequenceHelper;
import com.crypto.jtrade.core.provider.util.TradeSessionHelper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * A matcher of the symbol, not the default singleton mode, but a prototype, so that every time a bean is obtained, a
 * new bean is created.
 *
 * @author 0xWill
 **/
@Service
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SymbolMatcherImpl implements SymbolMatcher {

    private SymbolInfo symbolInfo;

    private int publishDepthSides = 0;

    private TreeMap<SellOrderKey, Order> sellOrders = new TreeMap<>();

    private TreeMap<BuyOrderKey, Order> buyOrders = new TreeMap<>();

    @Autowired
    private CoreConfig coreConfig;

    @Autowired
    private BeanMapping beanMapping;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private TradeRuleManager tradeRuleManager;

    @Autowired
    private PublicPublish publicPublish;

    @Autowired
    private PrivatePublish privatePublish;

    @Autowired
    private RedisLanding redisLanding;

    @Autowired
    private MySqlLanding mySqlLanding;

    /**
     * init the SymbolMatcher
     */
    @Override
    public void init(SymbolInfo symbolInfo) {
        this.symbolInfo = symbolInfo;
    }

    /**
     * place order
     */
    @Override
    public void placeOrder(Order order) {
        if (!checkOrder(order)) {
            orderCanceled(order);
            return;
        }

        BigDecimal fillPrice;
        BigDecimal fillQty;
        if (order.getSide() == OrderSide.BUY) {
            /**
             * buy order handler
             */
            if (order.getType() == OrderType.LIMIT) {
                Iterator<Order> iterator = sellOrders.values().iterator();
                while (iterator.hasNext()) {
                    Order sellOrder = iterator.next();
                    if (order.getPrice().compareTo(sellOrder.getPrice()) >= 0) {
                        /**
                         * The buy price is greater than the sell price, triggering the match
                         */
                        fillPrice = sellOrder.getPrice();
                        if (order.getLeftQty().compareTo(sellOrder.getLeftQty()) >= 0) {
                            /**
                             * The buy quantity is greater than the sell quantity, sell order is filled and removed from
                             * the sell set.
                             */
                            fillQty = sellOrder.getLeftQty();
                            iterator.remove();
                        } else {
                            /**
                             * The buy quantity is less than the sell quantity, buy order is filled.
                             */
                            fillQty = order.getLeftQty();
                        }
                        orderMatched(fillPrice, fillQty, order, MatchRole.TAKER, sellOrder, MatchRole.MAKER);

                        publishDepthSides = publishDepthSides | Constants.USE_BUY | Constants.USE_SELL;
                        if (order.getLeftQty().compareTo(BigDecimal.ZERO) == 0) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (order.getLeftQty().compareTo(BigDecimal.ZERO) > 0) {
                    /**
                     * Add to the buy collection
                     */
                    long priceLong = order.getPrice().movePointRight(Constants.MAX_DECIMAL).longValue();
                    BuyOrderKey buyOrderKey = new BuyOrderKey(priceLong, order.getOrderId());
                    buyOrders.put(buyOrderKey, order);
                    publishDepthSides = publishDepthSides | Constants.USE_BUY;
                }

            } else if (order.getType() == OrderType.MARKET) {
                Iterator<Order> iterator = sellOrders.values().iterator();
                while (iterator.hasNext()) {
                    Order sellOrder = iterator.next();
                    fillPrice = sellOrder.getPrice();
                    if (order.getLeftQty().compareTo(sellOrder.getLeftQty()) >= 0) {
                        /**
                         * The buy quantity is greater than the sell quantity, sell order is filled and removed from the
                         * sell set.
                         */
                        fillQty = sellOrder.getLeftQty();
                        iterator.remove();
                    } else {
                        /**
                         * The buy quantity is less than the sell quantity, buy order is filled.
                         */
                        fillQty = order.getLeftQty();
                    }
                    orderMatched(fillPrice, fillQty, order, MatchRole.TAKER, sellOrder, MatchRole.MAKER);

                    publishDepthSides = publishDepthSides | Constants.USE_BUY | Constants.USE_SELL;
                    if (order.getLeftQty().compareTo(BigDecimal.ZERO) == 0) {
                        break;
                    }
                }
                if (order.getLeftQty().compareTo(BigDecimal.ZERO) > 0) {
                    /**
                     * Cancel the left order
                     */
                    orderCanceled(order);
                }
            }
        } else {
            /**
             * sell order handler
             */
            if (order.getType() == OrderType.LIMIT) {
                Iterator<Order> iterator = buyOrders.values().iterator();
                while (iterator.hasNext()) {
                    Order buyOrder = iterator.next();
                    if (order.getPrice().compareTo(buyOrder.getPrice()) <= 0) {
                        /**
                         * The sell price is less than the buy price, triggering the match
                         */
                        fillPrice = buyOrder.getPrice();
                        if (order.getLeftQty().compareTo(buyOrder.getLeftQty()) >= 0) {
                            /**
                             * The sell quantity is greater than the buy quantity, buy order is filled and removed from
                             * the buy set.
                             */
                            fillQty = buyOrder.getLeftQty();
                            iterator.remove();
                        } else {
                            /**
                             * The sell quantity is less than the buy quantity, sell order is filled.
                             */
                            fillQty = order.getLeftQty();
                        }
                        orderMatched(fillPrice, fillQty, buyOrder, MatchRole.MAKER, order, MatchRole.TAKER);

                        publishDepthSides = publishDepthSides | Constants.USE_BUY | Constants.USE_SELL;
                        if (order.getLeftQty().compareTo(BigDecimal.ZERO) == 0) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
                if (order.getLeftQty().compareTo(BigDecimal.ZERO) > 0) {
                    /**
                     * Add to the sell collection
                     */
                    long priceLong = order.getPrice().movePointRight(Constants.MAX_DECIMAL).longValue();
                    SellOrderKey sellOrderKey = new SellOrderKey(priceLong, order.getOrderId());
                    sellOrders.put(sellOrderKey, order);
                    publishDepthSides = publishDepthSides | Constants.USE_SELL;
                }

            } else if (order.getType() == OrderType.MARKET) {
                Iterator<Order> iterator = buyOrders.values().iterator();
                while (iterator.hasNext()) {
                    Order buyOrder = iterator.next();
                    fillPrice = buyOrder.getPrice();
                    if (order.getLeftQty().compareTo(buyOrder.getLeftQty()) >= 0) {
                        /**
                         * The sell quantity is greater than the buy quantity, buy order is filled and removed from the
                         * buy set.
                         */
                        fillQty = buyOrder.getLeftQty();
                        iterator.remove();
                    } else {
                        /**
                         * The sell quantity is less than the buy quantity, sell order is filled.
                         */
                        fillQty = order.getLeftQty();
                    }
                    orderMatched(fillPrice, fillQty, buyOrder, MatchRole.MAKER, order, MatchRole.TAKER);

                    publishDepthSides = publishDepthSides | Constants.USE_BUY | Constants.USE_SELL;
                    if (order.getLeftQty().compareTo(BigDecimal.ZERO) == 0) {
                        break;
                    }
                }
                if (order.getLeftQty().compareTo(BigDecimal.ZERO) > 0) {
                    /**
                     * Cancel the left order
                     */
                    orderCanceled(order);
                }
            }
        }
    }

    /**
     * cancel order
     */
    @Override
    public void cancelOrder(Order order) {
        long priceLong = order.getPrice().movePointRight(Constants.MAX_DECIMAL).longValue();
        if (order.getSide() == OrderSide.BUY) {
            BuyOrderKey buyOrderKey = new BuyOrderKey(priceLong, order.getOrderId());
            if (buyOrders.remove(buyOrderKey) != null) {
                orderCanceled(order);
                // publish market
                publishDepthSides = publishDepthSides | Constants.USE_BUY;
            }
        } else {
            SellOrderKey sellOrderKey = new SellOrderKey(priceLong, order.getOrderId());
            if (sellOrders.remove(sellOrderKey) != null) {
                orderCanceled(order);
                // publish market
                publishDepthSides = publishDepthSides | Constants.USE_SELL;
            }
        }
    }

    /**
     * publish depth event
     */
    @Override
    public void publishDepthEvent() {
        if (publishDepthSides > 0) {
            /**
             * Cannot directly update the old depth, which may cause data inconsistency.
             */
            Depth oldDepth = localCache.getLastDepth(symbolInfo.getSymbol());
            Depth newDepth = new Depth(symbolInfo.getSymbol(), oldDepth.getBids(), oldDepth.getAsks());

            if ((publishDepthSides & Constants.USE_BUY) > 0) {
                newDepth.setBids(mergeDepthOnSide(buyOrders.values()));
            }
            if ((publishDepthSides & Constants.USE_SELL) > 0) {
                newDepth.setAsks(mergeDepthOnSide(sellOrders.values()));
            }
            // update the local cache
            localCache.setLastDepth(symbolInfo.getSymbol(), newDepth);
            // push depth
            publicPublish.publishDepth(newDepth);
            // reset to 0
            publishDepthSides = 0;
        }
    }

    /**
     * order matched
     */
    private void orderMatched(BigDecimal fillPrice, BigDecimal fillQty, Order buyOrder, MatchRole buyMatchRole,
        Order sellOrder, MatchRole sellMatchRole) {
        Long tradeId = SequenceHelper.incrementAndGetTradeId();
        ComplexEntity buyComplexEntity;
        ComplexEntity sellComplexEntity;

        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.ORDER_MATCHED);
        Lock buyLock = ClientLockHelper.getLock(buyOrder.getClientId());
        buyLock.lock();
        Lock sellLock = ClientLockHelper.getLock(sellOrder.getClientId());
        sellLock.lock();
        try {
            /**
             * buy matched
             */
            // get and init session
            TradeSession buySession = TradeSessionHelper.get();
            buySession.init(TradeType.STANDARD, buyOrder, localCache);
            // execute trade rule
            for (TradeRule rule : tradeRules) {
                rule.orderMatched(buySession, fillPrice, fillQty, buyMatchRole, tradeId);
            }
            OrderMatchedLanding buyLanding = buySession.getLanding();
            buyComplexEntity = buildComplexEntity(buyLanding);

            /**
             * sell matched
             */
            // get and init session
            TradeSession sellSession = TradeSessionHelper.get();
            sellSession.init(TradeType.STANDARD, sellOrder, localCache);
            // execute trade rule
            for (TradeRule rule : tradeRules) {
                rule.orderMatched(sellSession, fillPrice, fillQty, sellMatchRole, tradeId);
            }
            OrderMatchedLanding sellLanding = sellSession.getLanding();
            sellComplexEntity = buildComplexEntity(sellLanding);

            MatchedLandings landings = new MatchedLandings(buyLanding, sellLanding);
            /**
             * save result to redis
             */
            redisLanding.orderMatched(landings);
            /**
             * save result to mysql
             */
            mySqlLanding.orderMatched(landings);
        } finally {
            sellLock.unlock();
            buyLock.unlock();
        }

        /**
         * push matched
         */
        pushMatched(buyComplexEntity);
        pushMatched(sellComplexEntity);
    }

    /**
     * order canceled
     */
    private void orderCanceled(Order order) {
        List<TradeRule> tradeRules = tradeRuleManager.get(CommandIdentity.ORDER_CANCELED);
        Lock lock = ClientLockHelper.getLock(order.getClientId());
        lock.lock();
        try {
            // get and init session
            TradeSession session = TradeSessionHelper.get();
            session.init(TradeType.STANDARD, order, localCache);
            // execute trade rule
            for (TradeRule rule : tradeRules) {
                rule.orderCanceled(session);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * push matched
     */
    private void pushMatched(ComplexEntity complexEntity) {
        publicPublish.publishTrade(complexEntity.getTrade());
        privatePublish.publishComplex(complexEntity);
    }

    /**
     * check order
     */
    private boolean checkOrder(Order order) {
        if (order.getType() == OrderType.LIMIT) {
            if (order.getTimeInForce() == TimeInForce.GTX) {
                return checkGTX(order);
            } else if (order.getTimeInForce() == TimeInForce.FOK) {
                return checkFOK(order);
            }
        }
        return true;
    }

    /**
     * check GTX (Post Only)
     */
    private boolean checkGTX(Order order) {
        if (order.getSide() == OrderSide.BUY) {
            Map.Entry<SellOrderKey, Order> ask1Order = sellOrders.firstEntry();
            if (ask1Order != null) {
                BigDecimal ask1Price = ask1Order.getValue().getPrice();
                if (order.getPrice().compareTo(ask1Price) >= 0) {
                    return false;
                }
            }
        } else if (order.getSide() == OrderSide.SELL) {
            Map.Entry<BuyOrderKey, Order> bid1Order = buyOrders.firstEntry();
            if (bid1Order != null) {
                BigDecimal bid1Price = bid1Order.getValue().getPrice();
                if (order.getPrice().compareTo(bid1Price) <= 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * check FOK
     */
    private boolean checkFOK(Order order) {
        BigDecimal leftQty = order.getQuantity();
        if (order.getSide() == OrderSide.BUY) {
            for (Order askOrder : sellOrders.values()) {
                if (order.getPrice().compareTo(askOrder.getPrice()) >= 0) {
                    leftQty = leftQty.subtract(askOrder.getLeftQty());
                    if (leftQty.compareTo(BigDecimal.ZERO) <= 0) {
                        break;
                    }
                } else {
                    break;
                }
            }
        } else if (order.getSide() == OrderSide.SELL) {
            for (Order bidOrder : buyOrders.values()) {
                if (order.getPrice().compareTo(bidOrder.getPrice()) <= 0) {
                    leftQty = leftQty.subtract(bidOrder.getLeftQty());
                    if (leftQty.compareTo(BigDecimal.ZERO) <= 0) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        return leftQty.compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * merge depth on one side
     */
    private List<Depth.Item> mergeDepthOnSide(Collection<Order> orders) {
        List<Depth.Item> items = new ArrayList<>();
        BigDecimal price = BigDecimal.ZERO;
        BigDecimal qty = BigDecimal.ZERO;
        int maxDepths = coreConfig.getMaxDepths();
        int i = 0;
        int j = 0;
        boolean enough = false;
        for (Order order : orders) {
            if (i == 0) {
                price = order.getPrice();
                qty = order.getLeftQty();
            } else {
                if (order.getPrice().compareTo(price) != 0) {
                    items.add(new Depth.Item(price, qty));
                    price = order.getPrice();
                    qty = order.getLeftQty();
                    if (++j >= maxDepths) {
                        enough = true;
                        break;
                    }
                } else {
                    qty = qty.add(order.getLeftQty());
                }
            }
            i++;
        }
        if (i > 0 && !enough) {
            items.add(new Depth.Item(price, qty));
        }
        return items;
    }

    /**
     * build ComplexEntity
     */
    private ComplexEntity buildComplexEntity(OrderMatchedLanding landing) {
        List<Bill> billList = new ArrayList<>();
        if (landing.getProfitBill() != null) {
            billList.add(landing.getProfitBill());
        }
        if (landing.getFeeBill() != null) {
            billList.add(landing.getFeeBill());
        }
        return new ComplexEntity(landing.getOrder(), Collections.singletonList(landing.getBalance()),
            landing.getPosition(), landing.getTrade(), billList);
    }

    @Data
    @AllArgsConstructor
    private static final class SellOrderKey implements Comparable<SellOrderKey> {

        /**
         * To improve sorting performance, the type of price is changed from BigDecimal to long. Price multiplied by 10
         * to the 10th power.
         */
        private long price;

        /**
         * To improve sorting performance, the type of orderId is changed from Long to long.
         */
        private long orderId;

        @Override
        public int compareTo(SellOrderKey order) {
            if (this.price > order.price) {
                return 1;
            } else if (this.price < order.price) {
                return -1;
            } else {
                if (this.orderId > order.orderId) {
                    return 1;
                } else if (this.orderId < order.orderId) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }

    @Data
    @AllArgsConstructor
    private static final class BuyOrderKey implements Comparable<BuyOrderKey> {

        private long price;

        private long orderId;

        @Override
        public int compareTo(BuyOrderKey order) {
            if (this.price > order.price) {
                return -1;
            } else if (this.price < order.price) {
                return 1;
            } else {
                if (this.orderId > order.orderId) {
                    return 1;
                } else if (this.orderId < order.orderId) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }

}
