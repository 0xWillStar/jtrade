package com.crypto.jtrade.front.provider.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.util.ResponseHelper;
import com.crypto.jtrade.front.api.MarketApi;
import com.crypto.jtrade.front.provider.service.MarketService;

/**
 * market api controller
 *
 * @author 0xWill
 **/
@RestController
public class MarketController implements MarketApi {

    @Autowired
    private MarketService marketService;

    @Override
    public BaseResponse<JSONObject[]> getTickers() {
        return ResponseHelper.success(marketService.getTickers());
    }

    @Override
    public BaseResponse<JSONObject> getTicker(String symbol) {
        return ResponseHelper.success(marketService.getTicker(symbol));
    }

    @Override
    public BaseResponse<JSONObject> getDepth(String symbol, int limit) {
        return ResponseHelper.success(marketService.getDepth(symbol, limit));
    }

    @Override
    public BaseResponse<JSONArray[]> getKlines(String symbol, String interval, Long startTime, Long endTime,
        Long limit) {
        return ResponseHelper.success(marketService.getKlines(symbol, interval, startTime, endTime, limit));
    }

    @Override
    public BaseResponse<JSONObject[]> getTrades(String symbol, Long limit) {
        return ResponseHelper.success(marketService.getTrades(symbol, limit));
    }
}
