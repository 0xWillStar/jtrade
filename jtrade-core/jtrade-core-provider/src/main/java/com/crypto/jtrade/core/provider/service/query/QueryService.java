package com.crypto.jtrade.core.provider.service.query;

import java.util.List;

import com.crypto.jtrade.common.model.*;

/**
 * Query service
 *
 * @author 0xWill
 **/
public interface QueryService {

    /**
     * get symbol
     */
    SymbolInfo getSymbol(String symbol);

    /**
     * get symbols
     */
    List<SymbolInfo> getSymbols();

    /**
     * get symbol indicator
     */
    SymbolIndicator getSymbolIndicator(String symbol);

    /**
     * get last depth by the symbol
     */
    Depth getDepth(String symbol);

    /**
     * get last ticker by the symbol
     */
    Ticker getTicker(String symbol);

    /**
     * get last kline by the symbol
     */
    Kline getKline(String symbol, String period);

    /**
     * get open orders of the client
     */
    List<Order> getOpenOrders(String clientId);

    /**
     * get positions of the client
     */
    List<Position> getPositions(String clientId);

    /**
     * get asset balances of the client
     */
    List<AssetBalance> getBalances(String clientId);

}
