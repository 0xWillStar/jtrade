package com.crypto.jtrade.front.provider.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.front.api.model.IndexMarkFundingVO;
import com.crypto.jtrade.front.provider.cache.PublicCache;
import com.crypto.jtrade.front.provider.service.PublicService;

import lombok.extern.slf4j.Slf4j;

/**
 * front public service
 *
 * @author 0xWill
 */
@Service
@Slf4j
public class PublicServiceImpl implements PublicService {

    @Autowired
    private PublicCache publicCache;

    @Override
    public List<SymbolInfo> getInstruments(String symbol) {
        if (symbol == null) {
            return new ArrayList<>(publicCache.getAllSymbols().values());
        } else {
            return Collections.singletonList(publicCache.getSymbolInfo(symbol));
        }
    }

    @Override
    public IndexMarkFundingVO getIndexMarkFunding(String symbol) {
        JSONObject indexPrice = publicCache.getIndexPrice(symbol);
        JSONObject markPrice = publicCache.getMarkPrice(symbol);
        JSONObject fundingRate = publicCache.getFundingRate(symbol);

        return IndexMarkFundingVO.builder().symbol(symbol).indexPrice(indexPrice.getString("indexPrice"))
            .indexPriceTime(indexPrice.getLong("time")).markPrice(markPrice.getString("markPrice"))
            .markPriceTime(markPrice.getLong("time")).fundingRate(fundingRate.getString("fundingRate"))
            .fundingRateTime(fundingRate.getLong("time")).build();
    }
}
