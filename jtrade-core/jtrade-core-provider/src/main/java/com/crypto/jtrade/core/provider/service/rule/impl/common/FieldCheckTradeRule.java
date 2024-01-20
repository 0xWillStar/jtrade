package com.crypto.jtrade.core.provider.service.rule.impl.common;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.OrderSide;
import com.crypto.jtrade.common.constants.OrderType;
import com.crypto.jtrade.common.constants.SymbolStatus;
import com.crypto.jtrade.common.constants.TimeInForce;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.Depth;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.core.api.model.CancelOrderRequest;
import com.crypto.jtrade.core.api.model.PlaceOrderRequest;
import com.crypto.jtrade.core.provider.model.session.OrderSession;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.rule.impl.AbstractTradeRule;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * field check
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class FieldCheckTradeRule extends AbstractTradeRule {

    @Getter
    private int sequence = 2;

    @Getter
    private long usedProductType = Constants.USE_ALL_PRODUCT_TYPE;

    @Getter
    private long usedCommand =
        Constants.USE_PLACE_ORDER | Constants.USE_CANCEL_ORDER | Constants.USE_LIQUIDATION_CANCEL_ORDER;

    @Autowired
    private LocalCacheService localCache;

    /**
     * place order
     */
    @Override
    public void placeOrder(PlaceOrderRequest request, OrderSession session) {
        SymbolInfo symbolInfo = session.getAllSymbols().get(request.getSymbol());
        if (symbolInfo == null) {
            throw new TradeException(TradeError.SYMBOL_NOT_EXIST);
        }
        if (symbolInfo.getStatus() != SymbolStatus.CONTINUOUS) {
            throw new TradeException(TradeError.SYMBOL_CANNOT_TRADED);
        }
        /**
         * If the clientOrderId is not empty, it cannot be repeated in the open orders.
         */
        if (StringUtils.isNotEmpty(request.getClientOrderId())) {
            for (Order ord : session.getOrders().values()) {
                if (request.getClientOrderId().equals(ord.getClientOrderId())) {
                    throw new TradeException(TradeError.CLIENT_ORDER_ID_DUPLICATE);
                }
            }
        }

        /**
         * Limit Order
         */
        if (request.getType() == OrderType.LIMIT || request.getType() == OrderType.STOP
            || request.getType() == OrderType.TAKE_PROFIT) {
            // check priceTick
            if ((request.getPrice().divideAndRemainder(symbolInfo.getPriceTick()))[1].signum() != 0) {
                throw new TradeException(TradeError.PRICE_INVALID);
            }
            // check TimeInForce
            if (request.getTimeInForce() == TimeInForce.IOC) {
                throw new TradeException(TradeError.TIME_IN_FORCE_INVALID);
            }
            if (request.getType() == OrderType.LIMIT) {
                Depth lastDepth = localCache.getLastDepth(request.getSymbol());
                if (request.getTimeInForce() == TimeInForce.GTX) {
                    checkGTX(request, lastDepth);
                } else if (request.getTimeInForce() == TimeInForce.FOK) {
                    checkFOK(request, lastDepth);
                }
            }
        }

        /**
         * Market Order
         */
        if (request.getType() == OrderType.MARKET || request.getType() == OrderType.STOP_MARKET
            || request.getType() == OrderType.TAKE_PROFIT_MARKET) {
            // check TimeInForce
            if (request.getTimeInForce() != TimeInForce.IOC) {
                throw new TradeException(TradeError.TIME_IN_FORCE_INVALID);
            }
        }

        /**
         * check quantityTick
         */
        if ((request.getQuantity().divideAndRemainder(symbolInfo.getQuantityTick()))[1].signum() != 0) {
            throw new TradeException(TradeError.QUANTITY_INVALID);
        }
    }

    /**
     * cancel order
     */
    @Override
    public void cancelOrder(CancelOrderRequest request, OrderSession session) {
        Order order = null;
        if (request.getOrderId() != null) {
            order = session.getOrders().get(request.getOrderId());
        } else if (StringUtils.isNotEmpty(request.getClientOrderId())) {
            for (Order ord : session.getOrders().values()) {
                if (request.getClientOrderId().equals(ord.getClientOrderId())) {
                    order = ord;
                    break;
                }
            }
        }
        if (order == null || !order.getSymbol().equals(request.getSymbol())) {
            throw new TradeException(TradeError.ORDER_NOT_EXIST);
        }
        // save to session
        session.setOrder(order);
    }

    /**
     * check GTX (Post Only)
     */
    private void checkGTX(PlaceOrderRequest request, Depth lastDepth) {
        if (request.getSide() == OrderSide.BUY) {
            if (!CollectionUtils.isEmpty(lastDepth.getAsks())) {
                BigDecimal ask1Price = lastDepth.getAsks().get(0).getPrice();
                if (request.getPrice().compareTo(ask1Price) >= 0) {
                    throw new TradeException(TradeError.REJECT_ORDER);
                }
            }
        } else if (request.getSide() == OrderSide.SELL) {
            if (!CollectionUtils.isEmpty(lastDepth.getBids())) {
                BigDecimal bid1Price = lastDepth.getBids().get(0).getPrice();
                if (request.getPrice().compareTo(bid1Price) <= 0) {
                    throw new TradeException(TradeError.REJECT_ORDER);
                }
            }
        }
    }

    /**
     * check FOK
     */
    private void checkFOK(PlaceOrderRequest request, Depth lastDepth) {
        BigDecimal leftQty = request.getQuantity();
        if (request.getSide() == OrderSide.BUY) {
            for (Depth.Item item : lastDepth.getAsks()) {
                if (request.getPrice().compareTo(item.getPrice()) >= 0) {
                    leftQty = leftQty.subtract(item.getQuantity());
                    if (leftQty.compareTo(BigDecimal.ZERO) <= 0) {
                        break;
                    }
                } else {
                    break;
                }
            }
        } else if (request.getSide() == OrderSide.SELL) {
            for (Depth.Item item : lastDepth.getBids()) {
                if (request.getPrice().compareTo(item.getPrice()) <= 0) {
                    leftQty = leftQty.subtract(item.getQuantity());
                    if (leftQty.compareTo(BigDecimal.ZERO) <= 0) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        if (leftQty.compareTo(BigDecimal.ZERO) > 0) {
            throw new TradeException(TradeError.REJECT_ORDER);
        }
    }

}
