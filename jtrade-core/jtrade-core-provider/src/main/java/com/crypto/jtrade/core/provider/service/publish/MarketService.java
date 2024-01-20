package com.crypto.jtrade.core.provider.service.publish;

import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.model.Trade;
import com.crypto.jtrade.core.provider.service.rabbitmq.MessageClosure;

/**
 * market service
 *
 * @author 0xWill
 **/
public interface MarketService {

    void setMessageClosure(MessageClosure messageClosure);

    /**
     * add a new symbol to market service
     */
    void addSymbol(SymbolInfo symbolInfo);

    /**
     * trade processing
     */
    void tradeHandler(Trade trade);

}
