package com.crypto.jtrade.front.provider.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.crypto.jtrade.common.constants.KlinePeriod;
import com.crypto.jtrade.common.model.Kline;
import com.crypto.jtrade.common.util.Utils;
import com.crypto.jtrade.front.provider.cache.PublicCache;
import com.crypto.jtrade.front.provider.config.FrontConfig;
import com.crypto.jtrade.front.provider.mapper.KlineMapper;
import com.crypto.jtrade.front.provider.service.MarketService;

import lombok.extern.slf4j.Slf4j;

/**
 * front market service
 *
 * @author 0xWill
 */
@Service
@Slf4j
public class MarketServiceImpl implements MarketService {

    @Autowired
    private FrontConfig frontConfig;

    @Autowired
    private KlineMapper klineMapper;

    @Autowired
    private PublicCache publicCache;

    @Override
    public JSONObject[] getTickers() {
        return publicCache.getTickers();
    }

    @Override
    public JSONObject getTicker(String symbol) {
        return publicCache.getTicker(symbol);
    }

    @Override
    public JSONObject getDepth(String symbol, int limit) {
        return publicCache.getDepth(symbol);
    }

    @Override
    public JSONArray[] getKlines(String symbol, String interval, Long startTime, Long endTime, Long limit) {
        KlinePeriod klinePeriod = KlinePeriod.fromPeriod(interval);
        if (startTime == null && endTime == null) {
            return publicCache.getKlines(symbol, klinePeriod);
        } else {
            String tableName = klineMapper.getKlineTableName(symbol.toLowerCase(), frontConfig.getMarketDbSchema(),
                frontConfig.getSymbolDelimiter());
            List<Kline> klineList = klineMapper.getKlineList(symbol, klinePeriod.getPeriod(), startTime, endTime,
                tableName, frontConfig.getKlineDefaultSize());
            if (!CollectionUtils.isEmpty(klineList)) {
                JSONArray[] klineArray = new JSONArray[klineList.size()];
                for (int i = 0; i < klineList.size(); i++) {
                    Utils.formatKline(klineList.get(i), publicCache.getSymbolInfo(symbol));
                    String klineStr = klineList.get(i).toJSONString();
                    klineArray[i] = JSONArray.parseArray(klineStr);
                }
                return klineArray;
            } else {
                return new JSONArray[0];
            }
        }
    }

    @Override
    public JSONObject[] getTrades(String symbol, Long limit) {
        return publicCache.getTrades(symbol);
    }
}
