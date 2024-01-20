package com.crypto.jtrade.core.provider.service.funding.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import com.crypto.jtrade.core.provider.service.publish.PublicPublish;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.RedisOp;
import com.crypto.jtrade.common.constants.SymbolStatus;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.Depth;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.util.BigDecimalUtil;
import com.crypto.jtrade.common.util.TimerManager;
import com.crypto.jtrade.common.util.Utils;
import com.crypto.jtrade.core.api.model.MarkPriceRequest;
import com.crypto.jtrade.core.provider.model.landing.RedisOperation;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.cache.RedisService;
import com.crypto.jtrade.core.provider.service.funding.MarkPriceService;
import com.crypto.jtrade.core.provider.service.trade.TradeService;

import lombok.extern.slf4j.Slf4j;

/**
 * mark price service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
@ConditionalOnProperty(value = "jtrade.markPrice.enabled")
public class MarkPriceServiceImpl implements MarkPriceService {

    private static final int MA_MINUTES = 30;

    private static final BigDecimal MAX_PRICE_DEVIATE = new BigDecimal("0.0225");

    /**
     * price for MA, KEY1: symbol, KEY2: timestamp of the minute
     */
    private ConcurrentHashMap<String, Map<Long, BigDecimal>> lastMACache = new ConcurrentHashMap<>();

    private CountDownLatch initFinishLatch = new CountDownLatch(1);

    private ReentrantLock addLock = new ReentrantLock();

    @Value("${jtrade.markPrice.calculate-interval-seconds:3}")
    private int calculateIntervalSeconds;

    @Autowired
    private RedisService redisService;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private TradeService tradeService;

    @Autowired
    private PublicPublish publicPublish;

    @PostConstruct
    public void init() {
        long currTimeSeconds = initCalculateTimer();
        initMarkPrices(currTimeSeconds);
    }

    /**
     * add a new symbol to mark price service
     */
    @Override
    public void addSymbol(SymbolInfo symbolInfo) {
        // nothing to do
    }

    /**
     * init mark price
     */
    private void initMarkPrices(long currTimeSeconds) {
        List<RedisOperation> operationList = new ArrayList<>();
        long currBeginTime = Utils.getPeriodBeginTime(currTimeSeconds, 60);
        long minMaTime = currBeginTime - 60 * (MA_MINUTES - 1);

        localCache.getAllSymbols().values().parallelStream().forEach(symbolInfo -> {
            if (symbolInfo.getStatus() != SymbolStatus.CONTINUOUS) {
                return;
            }

            Map<Long, BigDecimal> lastMarkPrice = getLastMarkPrice(symbolInfo.getSymbol());
            String redisKey = Utils.format(Constants.REDIS_KEY_MARK_PRICE_MA, symbolInfo.getSymbol());
            Map<Object, Object> markPrices = redisService.getMarkPricesBySymbol(redisKey);
            for (Map.Entry<Object, Object> entry : markPrices.entrySet()) {
                long minuteTime = Long.parseLong((String)entry.getKey());
                BigDecimal markPrice = new BigDecimal((String)entry.getValue());

                if (minuteTime < minMaTime) {
                    // Expired need to be deleted from redis
                    addLock.lock();
                    try {
                        operationList
                            .add(new RedisOperation(redisKey, String.valueOf(minuteTime), null, true, RedisOp.HASH));
                    } finally {
                        addLock.unlock();
                    }
                } else {
                    lastMarkPrice.put(minuteTime, markPrice);
                }
            }
        });
        // save to redis
        if (!CollectionUtils.isEmpty(operationList)) {
            redisService.batchWriteOperations(operationList, false);
        }
        // latch countDown
        initFinishLatch.countDown();
    }

    /**
     * init calculate timer
     */
    private long initCalculateTimer() {
        long currTimeSeconds = Utils.currentSecondTime();
        long delay = (currTimeSeconds / calculateIntervalSeconds + 1) * calculateIntervalSeconds - currTimeSeconds;
        TimerManager.scheduleAtFixedRate(() -> onTimeCalculate(), delay, calculateIntervalSeconds, TimeUnit.SECONDS);
        return currTimeSeconds;
    }

    /**
     * time on calculate mark price
     */
    private void onTimeCalculate() {
        try {
            long currTimeSeconds = Utils.currentSecondTime();
            List<RedisOperation> operationList = new ArrayList<>();

            waitForInitFinished();
            boolean newMinute = Utils.isNewPeriod(currTimeSeconds, 60);
            long currBeginTime = 0;
            long deletedMinute = 0;
            if (newMinute) {
                currBeginTime = Utils.getPeriodBeginTime(currTimeSeconds, 60);
                deletedMinute = currBeginTime - 60 * MA_MINUTES;
            }

            final long finalDeletedMinute = deletedMinute;
            final long finalCurrBeginTime = currBeginTime;
            long remainHours = getRemainHours(currTimeSeconds);
            final BigDecimal timeRate = BigDecimalUtil.divide(BigDecimal.valueOf(remainHours),
                BigDecimal.valueOf(Constants.FUNDING_RATE_INTERVAL_HOURS));
            List<MarkPriceRequest> markPriceList = new ArrayList<>();
            localCache.getAllSymbols().values().parallelStream().forEach(symbolInfo -> {
                if (symbolInfo.getStatus() != SymbolStatus.CONTINUOUS) {
                    return;
                }

                Map<Long, BigDecimal> lastMarkPrice = getLastMarkPrice(symbolInfo.getSymbol());
                String redisKey = Utils.format(Constants.REDIS_KEY_MARK_PRICE_MA, symbolInfo.getSymbol());
                if (newMinute) {
                    BigDecimal priceMA = getPriceForMA(symbolInfo);
                    lastMarkPrice.remove(finalDeletedMinute);
                    if (priceMA != null) {
                        lastMarkPrice.put(finalCurrBeginTime, priceMA);
                    }
                    addLock.lock();
                    try {
                        operationList.add(
                            new RedisOperation(redisKey, String.valueOf(finalDeletedMinute), null, true, RedisOp.HASH));
                        if (priceMA != null) {
                            operationList.add(new RedisOperation(redisKey, String.valueOf(finalCurrBeginTime),
                                priceMA.toPlainString(), false, RedisOp.HASH));
                        }
                    } finally {
                        addLock.unlock();
                    }
                }
                /**
                 * calculate mark price
                 */
                BigDecimal markPrice = getMarkPrice(symbolInfo, timeRate);
                if (markPrice != null) {
                    MarkPriceRequest markPriceRequest =
                        new MarkPriceRequest(symbolInfo.getSymbol(), markPrice, System.currentTimeMillis());
                    addLock.lock();
                    try {
                        markPriceList.add(markPriceRequest);
                    } finally {
                        addLock.unlock();
                    }

                    /**
                     * publish mark price
                     */
                    publicPublish.publishMarkPrice(markPriceRequest);
                }
            });
            // set mark price
            if (!CollectionUtils.isEmpty(markPriceList)) {
                /**
                 * set mark price to trade service
                 */
                tradeService.setMarkPrice(markPriceList);
            }
            // save to redis
            if (!CollectionUtils.isEmpty(operationList)) {
                redisService.batchWriteOperations(operationList, false);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * wait for init finished
     */
    private void waitForInitFinished() {
        try {
            initFinishLatch.await();
        } catch (InterruptedException e) {
            throw new TradeException(e);
        }
    }

    /**
     * get last mark price by the symbol
     */
    private Map<Long, BigDecimal> getLastMarkPrice(String symbol) {
        /**
         * The computeIfAbsent has poor performance, can't use it.
         */
        Map<Long, BigDecimal> markPrices = lastMACache.get(symbol);
        if (markPrices == null) {
            markPrices = new HashMap<>();
            lastMACache.put(symbol, markPrices);
        }
        return markPrices;
    }

    /**
     * get price for MA
     */
    private BigDecimal getPriceForMA(SymbolInfo symbolInfo) {
        Depth depth = localCache.getLastDepth(symbolInfo.getSymbol());
        List<Depth.Item> bids = depth.getBids();
        List<Depth.Item> asks = depth.getAsks();
        BigDecimal indexPrice = localCache.getSymbolIndicator(symbolInfo.getSymbol()).getIndexPrice();
        if (!CollectionUtils.isEmpty(bids) && !CollectionUtils.isEmpty(asks) && indexPrice != null) {
            // (Bid1 + Ask1)/2 - Index_Price
            BigDecimal price =
                BigDecimalUtil.divide(bids.get(0).getPrice().add(asks.get(0).getPrice()), BigDecimal.valueOf(2));
            price = BigDecimalUtil.getVal(price.subtract(indexPrice), symbolInfo.getPriceAssetScale());
            return price;
        } else {
            return null;
        }
    }

    /**
     * get mark price
     */
    private BigDecimal getMarkPrice(SymbolInfo symbolInfo, BigDecimal timeRate) {
        BigDecimal indexPrice = localCache.getSymbolIndicator(symbolInfo.getSymbol()).getIndexPrice();
        BigDecimal fundingRate = localCache.getSymbolIndicator(symbolInfo.getSymbol()).getFundingRate();
        BigDecimal lastPrice = localCache.getLastTicker(symbolInfo.getSymbol()).getLastPrice();
        if (indexPrice != null && fundingRate != null && lastPrice != null) {
            // P_1 = Index_Price*(1 + Funding_Rate*(Remain_Time/8))
            BigDecimal price1 = indexPrice.multiply(BigDecimal.ONE.add(fundingRate.multiply(timeRate)));
            price1 = BigDecimalUtil.getVal(price1, symbolInfo.getPriceAssetScale());
            // P_2 = Index_Price + MA((Bid1 + Ask1)/2 - Index_Price, 30)
            BigDecimal price2 = indexPrice.add(getMA(symbolInfo));
            // Mid(P_1, P_2, contract_price)
            BigDecimal markPrice = getMiddle(price1, price2, lastPrice);

            // Deviate from the index price
            BigDecimal minPrice = indexPrice.multiply(BigDecimal.ONE.subtract(MAX_PRICE_DEVIATE));
            BigDecimal maxPrice = indexPrice.multiply(BigDecimal.ONE.add(MAX_PRICE_DEVIATE));
            if (markPrice.compareTo(minPrice) < 0 || markPrice.compareTo(maxPrice) > 0) {
                markPrice = price2;
            }
            return markPrice;
        } else {
            return null;
        }
    }

    /**
     * get MA
     */
    private BigDecimal getMA(SymbolInfo symbolInfo) {
        Map<Long, BigDecimal> prices = lastMACache.get(symbolInfo.getSymbol());
        if (!CollectionUtils.isEmpty(prices)) {
            BigDecimal sum = BigDecimal.ZERO;
            for (BigDecimal price : prices.values()) {
                sum = sum.add(price);
            }
            int size = prices.size();
            if (size > MA_MINUTES) {
                log.error("The size({}) of the MA in local cache for mark price is greater than {}", size, MA_MINUTES);
            }
            BigDecimal ma = BigDecimalUtil.divide(sum, BigDecimal.valueOf(size));
            return BigDecimalUtil.getVal(ma, symbolInfo.getPriceAssetScale());
        } else {
            return BigDecimal.ZERO;
        }
    }

    /**
     * get remain hours
     */
    private long getRemainHours(long currTimeSeconds) {
        long currBeginTime = Utils.getPeriodBeginTime(currTimeSeconds, Constants.FUNDING_RATE_INTERVAL_SECONDS);
        long nextBeginTime = currBeginTime + Constants.FUNDING_RATE_INTERVAL_SECONDS;
        return (nextBeginTime - currBeginTime) / Constants.FUNDING_RATE_INTERVAL_SECONDS;
    }

    /**
     * get middle
     */
    private BigDecimal getMiddle(BigDecimal price1, BigDecimal price2, BigDecimal lastPrice) {
        List<BigDecimal> list = new ArrayList<>();
        list.add(price1);
        list.add(price2);
        list.add(lastPrice);
        list.sort(BigDecimal::compareTo);
        return list.get(1);
    }
}
