package com.crypto.jtrade.core.provider.service.funding;

import com.crypto.jtrade.common.model.SymbolInfo;

/**
 * funding rate service
 *
 * @author 0xWill
 **/
public interface FundingRateService {

    /**
     * add a new symbol to funding rate service
     */
    void addSymbol(SymbolInfo symbolInfo);

}
