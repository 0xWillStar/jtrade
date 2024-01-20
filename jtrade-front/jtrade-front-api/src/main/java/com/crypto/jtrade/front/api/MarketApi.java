package com.crypto.jtrade.front.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.crypto.jtrade.common.model.BaseResponse;

/**
 * define market api
 *
 * @author 0xWill
 **/
public interface MarketApi {

    @GetMapping("/v1/market/tickers")
    BaseResponse<JSONObject[]> getTickers();

    @GetMapping("/v1/market/ticker")
    BaseResponse<JSONObject> getTicker(@RequestParam("symbol") String symbol);

    @GetMapping("/v1/market/depth")
    BaseResponse<JSONObject> getDepth(@RequestParam("symbol") String symbol,
        @RequestParam(value = "limit", required = false, defaultValue = "20") int limit);

    @GetMapping("/v1/market/klines")
    BaseResponse<JSONArray[]> getKlines(@RequestParam("symbol") String symbol,
        @RequestParam("interval") String interval, @RequestParam(value = "startTime", required = false) Long startTime,
        @RequestParam(value = "endTime", required = false) Long endTime,
        @RequestParam(value = "limit", required = false, defaultValue = "100") Long limit);

    @GetMapping("/v1/market/trades")
    BaseResponse<JSONObject[]> getTrades(@RequestParam("symbol") String symbol,
        @RequestParam(value = "limit", required = false, defaultValue = "100") Long limit);

}
