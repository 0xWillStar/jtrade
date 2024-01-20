package com.crypto.jtrade.core.provider.service.match;

import com.crypto.jtrade.common.model.SymbolInfo;

/**
 * match engine manager
 *
 * @author 0xWill
 **/
public interface MatchEngineManager {

    /**
     * add a new symbol to engine
     */
    void addSymbol(SymbolInfo symbolInfo);

    /**
     * get match engine by the symbol
     */
    MatchEngine getMatchEngine(String symbol);

}
