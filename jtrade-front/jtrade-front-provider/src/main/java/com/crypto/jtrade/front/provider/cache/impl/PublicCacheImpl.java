package com.crypto.jtrade.front.provider.cache.impl;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.IndicatorType;
import com.crypto.jtrade.common.constants.KlinePeriod;
import com.crypto.jtrade.common.constants.SymbolStatus;
import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.model.Depth;
import com.crypto.jtrade.common.model.FundingRate;
import com.crypto.jtrade.common.model.IndexPrice;
import com.crypto.jtrade.common.model.Kline;
import com.crypto.jtrade.common.model.MarkPrice;
import com.crypto.jtrade.common.model.SymbolIndicator;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.model.Ticker;
import com.crypto.jtrade.common.model.Trade;
import com.crypto.jtrade.common.util.Utils;
import com.crypto.jtrade.core.api.QueryApi;
import com.crypto.jtrade.front.provider.cache.PublicCache;
import com.crypto.jtrade.front.provider.cache.RedisService;
import com.crypto.jtrade.front.provider.config.FrontConfig;
import com.crypto.jtrade.front.provider.mapper.KlineMapper;
import com.crypto.jtrade.front.provider.mapper.TradeMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import lombok.extern.slf4j.Slf4j;

/**
 * public data cache
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class PublicCacheImpl implements PublicCache {

    private static final Object KEY = new Object();

    /**
     * Symbol cache, KEY1: Object, KEY2: SystemParameter
     */
    private LoadingCache<Object, Map<SystemParameter, String>> systemParameterCache =
        Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(5)).build(key -> findSystemParameters());

    /**
     * Symbol cache, KEY1: Object, KEY2: symbol
     */
    private LoadingCache<Object, Map<String, SymbolInfo>> symbolCache =
        Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(5)).build(key -> getSymbols());

    /**
     * Depth cache, KEY: symbol
     */
    private ConcurrentHashMap<String, JSONObject> lastDepths = new ConcurrentHashMap<>(Constants.MAX_SYMBOL_COUNT);

    /**
     * Ticker cache, KEY: symbol
     */
    private ConcurrentHashMap<String, JSONObject> lastTickers = new ConcurrentHashMap<>(Constants.MAX_SYMBOL_COUNT);

    /**
     * Kline cache, KEY: symbol
     */
    private ConcurrentHashMap<String, Map<KlinePeriod, CircularFifoQueue<JSONArray>>> klineCache =
        new ConcurrentHashMap<>(Constants.MAX_SYMBOL_COUNT);

    /**
     * Trade cache, KEY: symbol
     */
    private ConcurrentHashMap<String, CircularFifoQueue<JSONObject>> tradeCache =
        new ConcurrentHashMap<>(Constants.MAX_SYMBOL_COUNT);

    /**
     * MarkPrice cache, KEY: symbol
     */
    private ConcurrentHashMap<String, JSONObject> lastMarkPrices = new ConcurrentHashMap<>(Constants.MAX_SYMBOL_COUNT);

    /**
     * FundingRate cache, KEY: symbol
     */
    private ConcurrentHashMap<String, JSONObject> lastFundingRates =
        new ConcurrentHashMap<>(Constants.MAX_SYMBOL_COUNT);

    /**
     * IndexPrice cache, KEY: symbol
     */
    private ConcurrentHashMap<String, JSONObject> lastIndexPrices = new ConcurrentHashMap<>(Constants.MAX_SYMBOL_COUNT);

    @Autowired
    private FrontConfig frontConfig;

    @Autowired
    private RedisService redisService;

    @Autowired
    private KlineMapper klineMapper;

    @Autowired
    private TradeMapper tradeMapper;

    @Autowired
    private QueryApi queryApi;

    @PostConstruct
    public void init() {
        for (SymbolInfo symbolInfo : getAllSymbols().values()) {
            getTicker(symbolInfo.getSymbol());
        }
    }

    @Override
    public String getSystemParameter(SystemParameter parameter) {
        return systemParameterCache.get(KEY).get(parameter);
    }

    /**
     * get all symbols
     */
    @Override
    public Map<String, SymbolInfo> getAllSymbols() {
        return symbolCache.get(KEY);
    }

    /**
     * get symbol
     */
    @Override
    public SymbolInfo getSymbolInfo(String symbol) {
        return symbolCache.get(KEY).get(symbol);
    }

    /**
     * get depth by the symbol
     */
    @Override
    public JSONObject getDepth(String symbol) {
        JSONObject jsonObject = lastDepths.get(symbol);
        if (jsonObject == null) {
            BaseResponse<Depth> response = queryApi.getDepth(symbol);
            if (!response.isError()) {
                String depthStr = response.getData().toJSONString();
                jsonObject = JSONObject.parseObject(depthStr);
                lastDepths.put(symbol, jsonObject);
            }
        }
        return jsonObject;
    }

    /**
     * set depth by the symbol
     */
    @Override
    public void setDepth(String symbol, JSONObject depth) {
        lastDepths.put(symbol, depth);
    }

    /**
     * get ticker by the symbol
     */
    @Override
    public JSONObject getTicker(String symbol) {
        JSONObject jsonObject = lastTickers.get(symbol);
        if (jsonObject == null) {
            BaseResponse<Ticker> response = queryApi.getTicker(symbol);
            if (!response.isError()) {
                String tickerStr = response.getData().toJSONString();
                jsonObject = JSONObject.parseObject(tickerStr);
                lastTickers.put(symbol, jsonObject);
            }
        }
        return jsonObject;
    }

    @Override
    public JSONObject[] getTickers() {
        return lastTickers.values().toArray(new JSONObject[0]);
    }

    /**
     * set ticker by the symbol
     */
    @Override
    public void setTicker(String symbol, JSONObject ticker) {
        lastTickers.put(symbol, ticker);
    }

    /**
     * init kline cache
     */
    public void initKlineCache() {
        getAllSymbols().values().parallelStream().forEach(symbolInfo -> {
            if (symbolInfo.getStatus() != SymbolStatus.CONTINUOUS) {
                return;
            }
            Map<KlinePeriod, CircularFifoQueue<JSONArray>> klineMap = new ConcurrentHashMap<>();
            for (KlinePeriod period : KlinePeriod.values()) {
                CircularFifoQueue<JSONArray> klineQueue = new CircularFifoQueue<>(frontConfig.getKlineDefaultSize());
                // init kline list
                String tableName = klineMapper.getKlineTableName(symbolInfo.getSymbol().toLowerCase(),
                    frontConfig.getMarketDbSchema(), frontConfig.getSymbolDelimiter());
                long endTime = Utils.getPeriodBeginTime(Utils.currentSecondTime(), period.getSeconds());
                long startTime = endTime - frontConfig.getKlineDefaultSize() * period.getSeconds();
                List<Kline> klineList = klineMapper.getKlineList(symbolInfo.getSymbol(), period.getPeriod(), startTime,
                    endTime, tableName, frontConfig.getKlineDefaultSize());
                if (!CollectionUtils.isEmpty(klineList)) {
                    for (Kline kline : klineList) {
                        Utils.formatKline(kline, symbolInfo);
                        String klineStr = kline.toJSONString();
                        JSONArray jsonArray = JSONArray.parseArray(klineStr);
                        klineQueue.offer(jsonArray);
                    }
                }
                // update the last kline
                BaseResponse<Kline> response = queryApi.getKline(symbolInfo.getSymbol(), period.getPeriod());
                if (!response.isError()) {
                    Kline lastKline = response.getData();
                    if (lastKline != null) {
                        String klineStr = lastKline.toJSONString();
                        JSONArray jsonArray = JSONArray.parseArray(klineStr);
                        updateLastKline(klineQueue, jsonArray);
                    }
                }
                // update local cache
                klineMap.put(period, klineQueue);
            }
            klineCache.put(symbolInfo.getSymbol(), klineMap);
        });
    }

    /**
     * get kline by the symbol
     */
    @Override
    public JSONArray[] getKlines(String symbol, KlinePeriod period) {
        Map<KlinePeriod, CircularFifoQueue<JSONArray>> klineMap = klineCache.get(symbol);
        if (klineMap == null || klineMap.get(period) == null) {
            return new JSONArray[0];
        } else {
            return klineMap.get(period).toArray(new JSONArray[0]);
        }
    }

    /**
     * set kline by the symbol
     */
    @Override
    public void setKline(String symbol, KlinePeriod period, JSONArray kline) {
        Map<KlinePeriod, CircularFifoQueue<JSONArray>> klineMap = klineCache.get(symbol);
        if (klineMap == null) {
            klineMap = new ConcurrentHashMap<>();
            klineCache.put(symbol, klineMap);
        }
        CircularFifoQueue<JSONArray> klineQueue = klineMap.get(period);
        if (klineQueue == null) {
            klineQueue = new CircularFifoQueue<>(frontConfig.getKlineDefaultSize());
            klineMap.put(period, klineQueue);
        }
        updateLastKline(klineQueue, kline);
    }

    /**
     * init trade cache
     */
    public void initTradeCache() {
        getAllSymbols().values().parallelStream().forEach(symbolInfo -> {
            if (symbolInfo.getStatus() != SymbolStatus.CONTINUOUS) {
                return;
            }
            CircularFifoQueue<JSONObject> tradeQueue = new CircularFifoQueue<>(frontConfig.getTradeDefaultSize());
            // init trade list
            long startTime = System.currentTimeMillis() - frontConfig.getTradeDefaultHours() * 3600 * 1000;
            List<Trade> tradeList =
                tradeMapper.getTradeList(symbolInfo.getSymbol(), startTime, frontConfig.getTradeDefaultSize());
            if (!CollectionUtils.isEmpty(tradeList)) {
                for (Trade trade : tradeList) {
                    Utils.formatTrade(trade, symbolInfo);
                    String tradeStr = trade.toJSONString();
                    JSONObject jsonObject = JSONObject.parseObject(tradeStr);
                    tradeQueue.offer(jsonObject);
                }
            }
            // update local cache
            tradeCache.put(symbolInfo.getSymbol(), tradeQueue);
        });
    }

    /**
     * get trade by the symbol
     */
    @Override
    public JSONObject[] getTrades(String symbol) {
        CircularFifoQueue<JSONObject> tradeQueue = tradeCache.get(symbol);
        if (tradeQueue == null) {
            return new JSONObject[0];
        } else {
            return tradeQueue.toArray(new JSONObject[0]);
        }
    }

    /**
     * set trade by the symbol
     */
    @Override
    public void setTrade(String symbol, JSONObject trade) {
        CircularFifoQueue<JSONObject> tradeQueue = tradeCache.get(symbol);
        if (tradeQueue == null) {
            tradeQueue = new CircularFifoQueue<>(frontConfig.getTradeDefaultSize());
            tradeCache.put(symbol, tradeQueue);
        }
        if (tradeQueue.size() > 0) {
            JSONObject oldTrade = tradeQueue.get(tradeQueue.size() - 1);
            long oldTradeId = oldTrade.getLongValue("tradeId");
            long newTradeId = trade.getLongValue("tradeId");
            if (newTradeId > oldTradeId) {
                tradeQueue.offer(trade);
            }
        } else {
            tradeQueue.offer(trade);
        }
    }

    /**
     * get mark price by the symbol
     */
    @Override
    public JSONObject getMarkPrice(String symbol) {
        JSONObject markPrice = lastMarkPrices.get(symbol);
        if (markPrice == null) {
            initSymbolIndicator(symbol, IndicatorType.MARK_PRICE);
            markPrice = lastMarkPrices.get(symbol);
        }
        return markPrice;
    }

    /**
     * set mark price by the symbol
     */
    @Override
    public void setMarkPrice(String symbol, JSONObject markPrice) {
        lastMarkPrices.put(symbol, markPrice);
    }

    /**
     * get funding rate by the symbol
     */
    @Override
    public JSONObject getFundingRate(String symbol) {
        JSONObject fundingRate = lastFundingRates.get(symbol);
        if (fundingRate == null) {
            initSymbolIndicator(symbol, IndicatorType.FUNDING_RATE);
            fundingRate = lastFundingRates.get(symbol);
        }
        return fundingRate;
    }

    /**
     * set funding rate by the symbol
     */
    @Override
    public void setFundingRate(String symbol, JSONObject fundingRate) {
        lastFundingRates.put(symbol, fundingRate);
    }

    /**
     * get index price by the symbol
     */
    @Override
    public JSONObject getIndexPrice(String symbol) {
        JSONObject indexPrice = lastIndexPrices.get(symbol);
        if (indexPrice == null) {
            initSymbolIndicator(symbol, IndicatorType.INDEX_PRICE);
            indexPrice = lastIndexPrices.get(symbol);
        }
        return indexPrice;
    }

    /**
     * set index price by the symbol
     */
    @Override
    public void setIndexPrice(String symbol, JSONObject indexPrice) {
        lastIndexPrices.put(symbol, indexPrice);
    }

    /**
     * get symbols
     */
    private Map<String, SymbolInfo> getSymbols() {
        List<SymbolInfo> symbolList = queryApi.getSymbols().getData();
        return symbolList.stream().collect(Collectors.toConcurrentMap(SymbolInfo::getSymbol, symbol -> symbol));
    }

    /**
     * update the last kline
     */
    private void updateLastKline(CircularFifoQueue<JSONArray> klineQueue, JSONArray lastKline) {
        if (klineQueue.size() > 0) {
            JSONArray oldLast = klineQueue.get(klineQueue.size() - 1);
            long oldTime = oldLast.getLongValue(0);
            long newTime = lastKline.getLongValue(0);
            if (newTime > oldTime) {
                // add new value
                klineQueue.offer(lastKline);
            } else if (newTime == oldTime) {
                BigDecimal oldVolume = oldLast.getBigDecimal(5);
                BigDecimal newVolume = lastKline.getBigDecimal(5);
                if (newVolume.compareTo(oldVolume) > 0) {
                    // update old value
                    oldLast.clear();
                    oldLast.addAll(lastKline);
                }
            }
        } else {
            klineQueue.offer(lastKline);
        }
    }

    /**
     * find system parameters
     */
    private Map<SystemParameter, String> findSystemParameters() {
        return redisService.getSystemParameters();
    }

    /**
     * init symbol indicator
     */
    private void initSymbolIndicator(String symbol, IndicatorType indicatorType) {
        BaseResponse<SymbolIndicator> response = queryApi.getSymbolIndicator(symbol);
        if (!response.isError()) {
            SymbolIndicator indicator = response.getData();
            switch (indicatorType) {
                case INDEX_PRICE:
                    IndexPrice indexPrice = new IndexPrice();
                    indexPrice.setSymbol(symbol);
                    indexPrice.setIndexPrice(indicator.getIndexPrice());
                    lastIndexPrices.put(symbol, JSONObject.parseObject(indexPrice.toJSONString()));
                    break;
                case MARK_PRICE:
                    MarkPrice markPrice = new MarkPrice();
                    markPrice.setSymbol(symbol);
                    markPrice.setMarkPrice(indicator.getMarkPrice());
                    lastMarkPrices.put(symbol, JSONObject.parseObject(markPrice.toJSONString()));
                    break;
                case FUNDING_RATE:
                    FundingRate fundingRate = new FundingRate();
                    fundingRate.setSymbol(symbol);
                    fundingRate.setFundingRate(indicator.getFundingRate());
                    lastFundingRates.put(symbol, JSONObject.parseObject(fundingRate.toJSONString()));
                default:
                    break;
            }
        }
    }

}
