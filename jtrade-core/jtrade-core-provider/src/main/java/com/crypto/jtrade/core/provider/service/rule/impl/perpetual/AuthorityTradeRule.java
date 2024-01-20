package com.crypto.jtrade.core.provider.service.rule.impl.perpetual;

import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.core.api.model.CancelOrderRequest;
import com.crypto.jtrade.core.api.model.PlaceOrderRequest;
import com.crypto.jtrade.core.provider.model.session.OrderSession;
import com.crypto.jtrade.core.provider.service.rule.impl.AbstractTradeRule;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * authority check
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class AuthorityTradeRule extends AbstractTradeRule {

    @Getter
    private int sequence = 1;

    @Getter
    private long usedProductType = Constants.USE_PERPETUAL;

    @Getter
    private long usedCommand = Constants.USE_PLACE_ORDER | Constants.USE_CANCEL_ORDER;

    /**
     * place order
     */
    @Override
    public void placeOrder(PlaceOrderRequest request, OrderSession session) {
        // check trade authority
        if (request.getReduceOnly()) {
            if ((session.getTradeAuthority() & Constants.AUTH_CLOSE_POSITION) == 0) {
                throw new TradeException(TradeError.FORBID_CLOSE_POSITION);
            }
        } else {
            if ((session.getTradeAuthority() & Constants.AUTH_OPEN_POSITION) == 0) {
                throw new TradeException(TradeError.FORBID_OPEN_POSITION);
            }
        }
    }

    /**
     * cancel order
     */
    @Override
    public void cancelOrder(CancelOrderRequest request, OrderSession session) {
        // check trade authority
        if ((session.getTradeAuthority() & Constants.AUTH_CANCEL_ORDER) == 0) {
            throw new TradeException(TradeError.FORBID_CANCEL_ORDER);
        }
    }

}
