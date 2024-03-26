package com.crypto.jtrade.core.provider.service.rule.impl.perpetual;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.*;
import com.crypto.jtrade.common.model.ComplexEntity;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.core.api.model.CancelOrderRequest;
import com.crypto.jtrade.core.api.model.PlaceOrderRequest;
import com.crypto.jtrade.core.provider.model.convert.BeanMapping;
import com.crypto.jtrade.core.provider.model.landing.CancelOrderLanding;
import com.crypto.jtrade.core.provider.model.landing.PlaceOrderLanding;
import com.crypto.jtrade.core.provider.model.session.OrderSession;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.landing.MySqlLanding;
import com.crypto.jtrade.core.provider.service.landing.RedisLanding;
import com.crypto.jtrade.core.provider.service.publish.PrivatePublish;
import com.crypto.jtrade.core.provider.service.rule.impl.AbstractTradeRule;
import com.crypto.jtrade.core.provider.util.SequenceHelper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * when order type is stop or take profit
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class StopTradeRule extends AbstractTradeRule {

    @Getter
    private int sequence = 4;

    @Getter
    private long usedProductType = Constants.USE_PERPETUAL;

    @Getter
    private long usedCommand =
        Constants.USE_PLACE_ORDER | Constants.USE_CANCEL_ORDER | Constants.USE_TRIGGER_SECONDARY_ORDER;

    @Autowired
    private BeanMapping beanMapping;

    @Autowired
    private PrivatePublish privatePublish;

    @Autowired
    private RedisLanding redisLanding;

    @Autowired
    private MySqlLanding mySqlLanding;

    @Autowired
    private LocalCacheService localCache;

    /**
     * place order
     */
    @Override
    public void placeOrder(PlaceOrderRequest request, OrderSession session) {
        if ((request.getType() != OrderType.STOP) && (request.getType() != OrderType.STOP_MARKET)
            && (request.getType() != OrderType.TAKE_PROFIT) && (request.getType() != OrderType.TAKE_PROFIT_MARKET)) {
            return;
        }

        Order order = beanMapping.convert(request);
        order.setOrderTime(System.currentTimeMillis());
        order.setUpdateTime(order.getOrderTime());
        if (order.getOtoOrderType() == OTOOrderType.NONE || order.getOtoOrderType() == OTOOrderType.PRIMARY) {
            order.setStatus(OrderStatus.NEW);
            order.setOrderId(SequenceHelper.incrementAndGetOrderId());
            if (order.getOtoOrderType() == OTOOrderType.PRIMARY) {
                order.setSubOrderId1(session.getSubOrderId1());
                order.setSubOrderId2(session.getSubOrderId2());
            }
        } else {
            order.setStatus(OrderStatus.PENDING);
            order.setOrderId(session.getSubOrderId1());
            order.setSubOrderId1(session.getSubOrderId2());
        }

        /**
         * update local cache
         */
        session.setOrder(order);
        session.getOrders().put(order.getOrderId(), order);
        if (order.getStatus() == OrderStatus.NEW) {
            session.getStopOrderCache().addOrder(order);
        }

        /**
         * add the clientId to orderClientIds.
         */
        DataAction orderClientAction = DataAction.NONE;
        if (!localCache.getOrderClientIds().contains(order.getClientId())) {
            localCache.getOrderClientIds().add(order.getClientId());
            orderClientAction = DataAction.INSERT;
        }

        Order cpOrder = beanMapping.clone(order);
        /**
         * save landing to redis
         */
        PlaceOrderLanding landing = new PlaceOrderLanding(session.getRequestId(), cpOrder, DataAction.INSERT, null,
            null, null, null, orderClientAction, session.isStopTriggered());
        redisLanding.placeOrder(landing);
        /**
         * save landing to mysql
         */
        mySqlLanding.placeOrder(landing);
        /**
         * private publish
         */
        privatePublish.publishComplex(new ComplexEntity(cpOrder, null, null, null, null));
    }

    /**
     * cancel order
     */
    @Override
    public void cancelOrder(CancelOrderRequest request, OrderSession session) {
        Order order = session.getOrder();
        if ((order.getType() != OrderType.STOP) && (order.getType() != OrderType.STOP_MARKET)
            && (order.getType() != OrderType.TAKE_PROFIT) && (order.getType() != OrderType.TAKE_PROFIT_MARKET)) {
            return;
        }

        order.setUpdateTime(System.currentTimeMillis());
        OrderStatus oldStatus = order.getStatus();
        if (session.isStopRejected()) {
            order.setStatus(OrderStatus.REJECTED);
        } else {
            order.setStatus(OrderStatus.CANCELED);
        }

        /**
         * remove the order from local cache.
         */
        session.getClientEntity().removeOrder(order.getOrderId());
        if (!session.isStopRejected() && oldStatus == OrderStatus.NEW) {
            session.getStopOrderCache().removeOrder(order);
        }

        /**
         * If the orders of the client is empty, remove the clientId from orderClientIds.
         */
        DataAction orderClientAction = DataAction.NONE;
        if (session.getClientEntity().getOrders().isEmpty()) {
            localCache.getOrderClientIds().remove(order.getClientId());
            orderClientAction = DataAction.DELETE;
        }

        /**
         * save landing to redis
         */
        CancelOrderLanding landing = new CancelOrderLanding(session.getRequestId(), order, orderClientAction);
        redisLanding.cancelOrder(landing);
        /**
         * save landing to mysql
         */
        mySqlLanding.cancelOrder(landing);
        /**
         * private publish
         */
        privatePublish.publishComplex(new ComplexEntity(order, null, null, null, null));
    }

    /**
     * trigger secondary order
     */
    @Override
    public void triggerSecondaryOrder(Long requestId, Order order, OrderSession session) {
        if (((order.getType() != OrderType.STOP) && (order.getType() != OrderType.STOP_MARKET)
            && (order.getType() != OrderType.TAKE_PROFIT) && (order.getType() != OrderType.TAKE_PROFIT_MARKET))
            || order.getOtoOrderType() != OTOOrderType.SECONDARY) {
            return;
        }

        if (session.getOrders().get(order.getOrderId()) == null) {
            return;
        }

        order.setStatus(OrderStatus.NEW);
        /**
         * update local cache
         */
        session.getOrders().put(order.getOrderId(), order);
        session.getStopOrderCache().addOrder(order);

        Order cpOrder = beanMapping.clone(order);
        /**
         * save landing to redis
         */
        PlaceOrderLanding landing = new PlaceOrderLanding(session.getRequestId(), cpOrder, DataAction.UPDATE, null,
            null, null, null, DataAction.NONE, session.isStopTriggered());
        redisLanding.placeOrder(landing);
        /**
         * save landing to mysql
         */
        mySqlLanding.placeOrder(landing);
        /**
         * private publish
         */
        privatePublish.publishComplex(new ComplexEntity(cpOrder, null, null, null, null));
    }

}
