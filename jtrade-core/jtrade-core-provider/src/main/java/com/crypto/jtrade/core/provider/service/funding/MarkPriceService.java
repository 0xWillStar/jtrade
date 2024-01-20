package com.crypto.jtrade.core.provider.service.funding;

import com.crypto.jtrade.common.model.SymbolInfo;

/**
 * mark price service
 *
 * @author 0xWill
 **/
public interface MarkPriceService {

    /**
     * add a new symbol to mark price service
     */
    void addSymbol(SymbolInfo symbolInfo);

}
