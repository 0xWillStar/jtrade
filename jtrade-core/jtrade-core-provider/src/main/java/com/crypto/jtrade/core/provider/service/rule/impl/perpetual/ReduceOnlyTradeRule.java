package com.crypto.jtrade.core.provider.service.rule.impl.perpetual;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.OrderSide;
import com.crypto.jtrade.common.constants.OrderType;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.util.BigDecimalUtil;
import com.crypto.jtrade.core.api.model.PlaceOrderRequest;
import com.crypto.jtrade.core.provider.model.session.OrderSession;
import com.crypto.jtrade.core.provider.service.rule.impl.AbstractTradeRule;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * when reduce only is true
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class ReduceOnlyTradeRule extends AbstractTradeRule {

    @Getter
    private int sequence = 3;

    @Getter
    private long usedProductType = Constants.USE_PERPETUAL;

    @Getter
    private long usedCommand = Constants.USE_PLACE_ORDER;

    /**
     * place order
     */
    @Override
    public void placeOrder(PlaceOrderRequest request, OrderSession session) {
        if (((request.getType() != OrderType.LIMIT) && (request.getType() != OrderType.MARKET))
            || !request.getReduceOnly()) {
            return;
        }

        Position position = session.getPositions().get(request.getSymbol());
        if (position == null || position.getPositionAmt().compareTo(BigDecimal.ZERO) == 0) {
            throw new TradeException(TradeError.REDUCE_ONLY_INVALID);
        }

        BigDecimal positionAmt = position.getPositionAmt();
        if ((positionAmt.compareTo(BigDecimal.ZERO) > 0 && request.getSide() == OrderSide.BUY)
            || (positionAmt.compareTo(BigDecimal.ZERO) < 0 && request.getSide() == OrderSide.SELL)) {
            throw new TradeException(TradeError.REDUCE_ONLY_INVALID);
        }

        BigDecimal totalQty = BigDecimal.ZERO;
        /**
         * There may be market orders in the client's order list.
         */
        for (Order order : session.getOrders().values()) {
            if (order.getSymbol().equals(request.getSymbol()) && order.getSide() == request.getSide()) {
                if (order.getType() == OrderType.MARKET || (request.getType() == OrderType.LIMIT
                    && ((request.getSide() == OrderSide.SELL && order.getPrice().compareTo(request.getPrice()) <= 0)
                        || (request.getSide() == OrderSide.BUY
                            && order.getPrice().compareTo(request.getPrice()) >= 0)))) {
                    totalQty = totalQty.add(order.getLeftQty());
                }
            }
        }
        positionAmt = positionAmt.abs();
        if (totalQty.compareTo(positionAmt) >= 0) {
            throw new TradeException(TradeError.REDUCE_ONLY_INVALID);
        } else {
            request.setQuantity(BigDecimalUtil.min(request.getQuantity(), positionAmt.subtract(totalQty)));
        }
    }

}
