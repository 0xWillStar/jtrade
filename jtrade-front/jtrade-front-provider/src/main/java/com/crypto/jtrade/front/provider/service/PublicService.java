package com.crypto.jtrade.front.provider.service;

import java.util.List;

import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.front.api.model.IndexMarkFundingVO;

/**
 * public service
 *
 * @author 0xWill
 **/
public interface PublicService {

    List<SymbolInfo> getInstruments(String symbol);

    IndexMarkFundingVO getIndexMarkFunding(String symbol);

}
