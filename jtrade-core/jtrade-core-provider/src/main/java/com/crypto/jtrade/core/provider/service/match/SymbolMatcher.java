package com.crypto.jtrade.core.provider.service.match;

import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.SymbolInfo;

/**
 * symbol matcher
 *
 * @author 0xWill
 **/
public interface SymbolMatcher {

    /**
     * init the SymbolMatcher
     */
    void init(SymbolInfo symbolInfo);

    /**
     * place order
     */
    void placeOrder(Order order);

    /**
     * cancel order
     */
    void cancelOrder(Order order);

    /**
     * publish depth event
     */
    void publishDepthEvent();

}
