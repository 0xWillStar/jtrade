package com.crypto.jtrade.front.provider.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * market service
 *
 * @author 0xWill
 **/
public interface MarketService {

    JSONObject[] getTickers();

    JSONObject getTicker(String symbol);

    JSONObject getDepth(String symbol, int limit);

    JSONArray[] getKlines(String symbol, String interval, Long startTime, Long endTime, Long limit);

    JSONObject[] getTrades(String symbol, Long limit);

}
