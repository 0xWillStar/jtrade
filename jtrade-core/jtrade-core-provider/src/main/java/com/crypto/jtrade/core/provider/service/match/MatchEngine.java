package com.crypto.jtrade.core.provider.service.match;

import java.util.List;

import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.core.api.model.EmptyRequest;
import com.crypto.jtrade.core.provider.model.liquidation.LiquidationCanceledOrder;

/**
 * match engine
 *
 * @author 0xWill
 **/
public interface MatchEngine {

    /**
     * get the symbols
     */
    List<SymbolInfo> getSymbols();

    /**
     * add a new symbol to the engine
     */
    void addSymbol(SymbolInfo symbolInfo);

    /**
     * init the MatchEngine
     */
    void init(int engineId, List<SymbolInfo> symbols, NamedThreadFactory threadFactory);

    /**
     * empty command
     */
    void emptyCommand(EmptyRequest request);

    /**
     * place order
     */
    void placeOrder(Order order);

    /**
     * cancel order
     */
    void cancelOrder(Order order);

    /**
     * liquidation cancel order
     */
    void liquidationCancelOrder(LiquidationCanceledOrder liquidationCanceledOrder);

    /**
     * load order
     */
    void loadOrder(Order order);

    /**
     * publish depth event
     */
    void publishDepthEvent();

}
