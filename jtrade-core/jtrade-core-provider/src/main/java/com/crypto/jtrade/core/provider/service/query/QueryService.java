package com.crypto.jtrade.core.provider.service.query;

import java.util.List;

import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.Depth;
import com.crypto.jtrade.common.model.Kline;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.SymbolIndicator;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.model.Ticker;

/**
 * Query service
 *
 * @author 0xWill
 **/
public interface QueryService {

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
