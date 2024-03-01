package com.crypto.jtrade.core.provider.service.funding.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.RedisOp;
import com.crypto.jtrade.common.constants.SymbolStatus;
import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.Depth;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.util.BigDecimalUtil;
import com.crypto.jtrade.common.util.TimerManager;
import com.crypto.jtrade.common.util.Utils;
import com.crypto.jtrade.core.api.model.FundingRateRequest;
import com.crypto.jtrade.core.provider.model.landing.RedisOperation;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.cache.RedisService;
import com.crypto.jtrade.core.provider.service.funding.FundingRateService;
import com.crypto.jtrade.core.provider.service.publish.PublicPublish;
import com.crypto.jtrade.core.provider.service.trade.TradeCommand;

import lombok.extern.slf4j.Slf4j;

/**
 * funding rate service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
@ConditionalOnProperty(value = "jtrade.fundingRate.enabled")
public class FundingRateServiceImpl implements FundingRateService {

    private static final BigDecimal DEFAULT_FUNDING_RATE = new BigDecimal("0.0001");

    private static final int MIN_PREMIUM_COUNT = 1;

    private static final int FUNDING_RATE_SCALE = 6;

    private static final BigDecimal INTEREST_RATE = new BigDecimal("0.0001");

    private CountDownLatch initFinishLatch = new CountDownLatch(1);

    private ReentrantLock addLock = new ReentrantLock();

    /**
     * Premium cache, KEY: symbol
     */
    private ConcurrentHashMap<String, List<BigDecimal>> lastPremiumCache = new ConcurrentHashMap<>();

    @Autowired
    private RedisService redisService;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private TradeCommand tradeCommand;

    @Autowired
    private PublicPublish publicPublish;

    @PostConstruct
    public void init() {
        long currTimeSeconds = initOneMinTimer();
        initPremiums(currTimeSeconds);
    }

    /**
     * add a new symbol to funding rate service
     */
    @Override
    public void addSymbol(SymbolInfo symbolInfo) {
        // nothing to do
    }

    /**
     * init Premiums
     */
    private void initPremiums(long currTimeSeconds) {
        List<RedisOperation> operationList = new ArrayList<>();
        localCache.getAllSymbols().values().parallelStream().forEach(symbolInfo -> {
            if (symbolInfo.getStatus() != SymbolStatus.CONTINUOUS) {
                return;
            }

            List<String> premiumList = redisService.getPremiumsBySymbol(symbolInfo.getSymbol());
            /**
             * The first value of the list is the beginning time of the period. If the beginning time is not equal the
             * beginning time of the current period, the premiumList is expired.
             */
            boolean needInitRedis = false;
            long currBeginTime = Utils.getPeriodBeginTime(currTimeSeconds, Constants.FUNDING_RATE_INTERVAL_SECONDS);
            if (!CollectionUtils.isEmpty(premiumList)) {
                long beginTimeCached = Long.parseLong(premiumList.get(0));
                if (beginTimeCached == currBeginTime) {
                    List<BigDecimal> lastPremium = getLastPremium(symbolInfo.getSymbol());
                    int i = 0;
                    for (String premium : premiumList) {
                        if (i++ > 0) {
                            lastPremium.add(new BigDecimal(premium));
                        }
                    }
                } else {
                    needInitRedis = true;
                }
            } else {
                needInitRedis = true;
            }

            if (needInitRedis) {
                String redisKey = Utils.format(Constants.REDIS_KEY_PREMIUM, symbolInfo.getSymbol());
                addLock.lock();
                try {
                    operationList.add(new RedisOperation(redisKey, null, null, true, RedisOp.LIST));
                    operationList
                        .add(new RedisOperation(redisKey, null, String.valueOf(currBeginTime), false, RedisOp.LIST));
                } finally {
                    addLock.unlock();
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
     * init one minute timer
     */
    private long initOneMinTimer() {
        long currTimeSeconds = Utils.currentSecondTime();
        int interval = 60;
        long delay = (currTimeSeconds / interval + 1) * interval - currTimeSeconds;
        TimerManager.scheduleAtFixedRate(() -> onTimeOneMin(), delay, interval, TimeUnit.SECONDS);
        return currTimeSeconds;
    }

    /**
     * time to one minute
     */
    private void onTimeOneMin() {
        try {
            long currTimeSeconds = Utils.currentSecondTime();
            List<RedisOperation> operationList = new ArrayList<>();

            waitForInitFinished();
            boolean newPeriod = Utils.isNewPeriod(currTimeSeconds, Constants.FUNDING_RATE_INTERVAL_SECONDS);
            long currBeginTime = Utils.getPeriodBeginTime(currTimeSeconds, Constants.FUNDING_RATE_INTERVAL_SECONDS);
            List<FundingRateRequest> fundingRateList = new ArrayList<>();
            localCache.getAllSymbols().values().parallelStream().forEach(symbolInfo -> {
                if (symbolInfo.getStatus() != SymbolStatus.CONTINUOUS) {
                    return;
                }

                List<BigDecimal> lastPremium = getLastPremium(symbolInfo.getSymbol());
                String redisKey = Utils.format(Constants.REDIS_KEY_PREMIUM, symbolInfo.getSymbol());
                BigDecimal premium = getPremium(symbolInfo);
                if (premium != null) {
                    lastPremium.add(premium);
                }
                BigDecimal fundingRate = getFundingRate(lastPremium, symbolInfo.getMaintenanceMarginRate());
                /**
                 * update funding rate of the local cache
                 */
                localCache.getSymbolIndicator(symbolInfo.getSymbol()).setFundingRate(fundingRate);

                /**
                 * publish funding rate
                 */
                FundingRateRequest fundingRateRequest =
                    new FundingRateRequest(symbolInfo.getSymbol(), fundingRate, System.currentTimeMillis());
                publicPublish.publishFundingRate(fundingRateRequest);

                if (newPeriod) {
                    // reinitialize premium list in redis
                    addLock.lock();
                    try {
                        operationList.add(new RedisOperation(redisKey, null, null, true, RedisOp.LIST));
                        operationList.add(
                            new RedisOperation(redisKey, null, String.valueOf(currBeginTime), false, RedisOp.LIST));
                        fundingRateList.add(fundingRateRequest);
                    } finally {
                        addLock.unlock();
                    }
                    // reset lastPremium
                    lastPremium.clear();

                    // TODO: save fundingRate to database

                } else if (premium != null) {
                    addLock.lock();
                    try {
                        operationList
                            .add(new RedisOperation(redisKey, "", premium.toPlainString(), false, RedisOp.LIST));
                    } finally {
                        addLock.unlock();
                    }
                }
            });
            // set funding rate
            if (!CollectionUtils.isEmpty(fundingRateList)) {
                /**
                 * set funding rate to trade service which calculating funding fee
                 */
                tradeCommand.setFundingRate(fundingRateList);
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
     * get funding rate
     */
    private BigDecimal getFundingRate(List<BigDecimal> lastPremium, BigDecimal maintenanceMarginRate) {
        BigDecimal fundingRate = DEFAULT_FUNDING_RATE;
        if (lastPremium.size() >= MIN_PREMIUM_COUNT) {
            BigDecimal sumPremium = BigDecimal.ZERO;
            int sumIdx = 0;
            for (int i = 1; i <= lastPremium.size(); i++) {
                sumPremium = sumPremium.add(lastPremium.get(i - 1).multiply(BigDecimal.valueOf(i)));
                sumIdx = sumIdx + i;
            }
            BigDecimal avgPremium = BigDecimalUtil.getVal(BigDecimalUtil.divide(sumPremium, BigDecimal.valueOf(sumIdx)),
                FUNDING_RATE_SCALE);
            BigDecimal boundary = new BigDecimal(localCache.getSystemParameter(SystemParameter.FUNDING_RATE_BOUNDARY));
            // r = Premium + clamp(Interest Rate-Premium, -D, D)
            fundingRate = avgPremium.add(BigDecimalUtil.getClamp(INTEREST_RATE.subtract(avgPremium), boundary));
            // R = clamp(r, -0.75*maintenanceMarginRate，0.75*maintenanceMarginRate)
            BigDecimal mmrBoundary =
                new BigDecimal(localCache.getSystemParameter(SystemParameter.FUNDING_RATE_MMR_BOUNDARY));
            fundingRate = BigDecimalUtil.getClamp(fundingRate, mmrBoundary.multiply(maintenanceMarginRate));
        }
        return fundingRate;
    }

    /**
     * get the premium of the symbol.
     * 
     * Premium = (Max(0, Impact bid price – index price) - Max(0, Index Price - Impact Ask Price)) / Index Price
     */
    private BigDecimal getPremium(SymbolInfo symbolInfo) {
        BigDecimal premium = null;
        BigDecimal indexPrice = localCache.getSymbolIndicator(symbolInfo.getSymbol()).getIndexPrice();

        BigDecimal inv = symbolInfo.getImpactValue().multiply(symbolInfo.getDefaultLeverage());
        Depth depth = localCache.getLastDepth(symbolInfo.getSymbol());
        BigDecimal bidImpactPrice = getImpactPrice(inv, depth.getBids());
        BigDecimal askImpactPrice = getImpactPrice(inv, depth.getAsks());

        if (indexPrice != null && bidImpactPrice != null && askImpactPrice != null) {
            BigDecimal bidPrice = BigDecimalUtil.max(BigDecimal.ZERO, bidImpactPrice.subtract(indexPrice));
            BigDecimal askPrice = BigDecimalUtil.max(BigDecimal.ZERO, indexPrice.subtract(askImpactPrice));
            premium = BigDecimalUtil.divide(bidPrice.subtract(askPrice), indexPrice);
        }
        return premium;
    }

    /**
     * get impact price
     */
    private BigDecimal getImpactPrice(BigDecimal inv, List<Depth.Item> depth) {
        BigDecimal sumTotal = BigDecimal.ZERO;
        BigDecimal sumQty = BigDecimal.ZERO;
        BigDecimal impactPrice = null;
        for (Depth.Item item : depth) {
            BigDecimal total = item.getPrice().multiply(item.getQuantity());
            if (sumTotal.add(total).compareTo(inv) >= 0) {
                sumQty = sumQty.add(BigDecimalUtil.divide(inv.subtract(sumTotal), item.getPrice()));
                impactPrice = BigDecimalUtil.divide(inv, sumQty);
                break;
            } else {
                sumTotal = sumTotal.add(total);
                sumQty = sumQty.add(item.getQuantity());
            }
        }
        return impactPrice;
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
     * get last premium by the symbol
     */
    private List<BigDecimal> getLastPremium(String symbol) {
        /**
         * The computeIfAbsent has poor performance, can't use it.
         */
        List<BigDecimal> premiums = lastPremiumCache.get(symbol);
        if (premiums == null) {
            premiums = new ArrayList<>();
            lastPremiumCache.put(symbol, premiums);
        }
        return premiums;
    }

}
