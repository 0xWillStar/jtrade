package com.crypto.jtrade.front.provider.cache;

import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.crypto.jtrade.common.constants.KlinePeriod;
import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.model.SymbolInfo;

public interface PublicCache {

    /**
     * get system parameter
     */
    String getSystemParameter(SystemParameter parameter);

    /**
     * get all symbols
     */
    Map<String, SymbolInfo> getAllSymbols();

    /**
     * get symbol
     */
    SymbolInfo getSymbolInfo(String symbol);

    /**
     * get depth by the symbol
     */
    JSONObject getDepth(String symbol);

    /**
     * set depth by the symbol
     */
    void setDepth(String symbol, JSONObject depth);

    /**
     * get ticker by the symbol
     */
    JSONObject getTicker(String symbol);

    /**
     * get all tickers
     */
    JSONObject[] getTickers();

    /**
     * set ticker by the symbol
     */
    void setTicker(String symbol, JSONObject ticker);

    /**
     * init kline cache
     */
    void initKlineCache();

    /**
     * get kline by the symbol
     */
    JSONArray[] getKlines(String symbol, KlinePeriod period);

    /**
     * set kline by the symbol
     */
    void setKline(String symbol, KlinePeriod period, JSONArray kline);

    /**
     * init trade cache
     */
    void initTradeCache();

    /**
     * get trade by the symbol
     */
    JSONObject[] getTrades(String symbol);

    /**
     * set trade by the symbol
     */
    void setTrade(String symbol, JSONObject trade);

    /**
     * get mark price by the symbol
     */
    JSONObject getMarkPrice(String symbol);

    /**
     * set mark price by the symbol
     */
    void setMarkPrice(String symbol, JSONObject markPrice);

    /**
     * get funding rate by the symbol
     */
    JSONObject getFundingRate(String symbol);

    /**
     * set funding rate by the symbol
     */
    void setFundingRate(String symbol, JSONObject fundingRate);

    /**
     * get index price by the symbol
     */
    JSONObject getIndexPrice(String symbol);

    /**
     * set index price by the symbol
     */
    void setIndexPrice(String symbol, JSONObject indexPrice);

}
