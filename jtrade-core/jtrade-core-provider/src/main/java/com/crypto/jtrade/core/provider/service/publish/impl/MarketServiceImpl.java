package com.crypto.jtrade.core.provider.service.publish.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.crypto.jtrade.common.constants.*;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.*;
import com.crypto.jtrade.common.util.StreamUtils;
import com.crypto.jtrade.common.util.TimerManager;
import com.crypto.jtrade.common.util.Utils;
import com.crypto.jtrade.core.provider.config.CoreConfig;
import com.crypto.jtrade.core.provider.mapper.KlineMapper;
import com.crypto.jtrade.core.provider.model.convert.BeanMapping;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.publish.MarketService;
import com.crypto.jtrade.core.provider.service.rabbitmq.MessageClosure;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * market service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class MarketServiceImpl implements MarketService {

    private static final int ONE_MIN_NUMBER = 1439;

    private static final int ONE_HOUR_NUMBER = 23;

    /**
     * latches for whether the init kline is finished KEY: symbol
     */
    private ConcurrentHashMap<String, CountDownLatch> initFinishLatches = new ConcurrentHashMap<>();

    /**
     * one minute kline cache for last tickers, cache 1439 records per symbol KEY: symbol
     */
    private ConcurrentHashMap<String, List<Kline>> oneMinKlineCache = new ConcurrentHashMap<>();

    /**
     * one hour kline cache for last tickers, cache 23 records per symbol KEY: symbol
     */
    private ConcurrentHashMap<String, List<Kline>> oneHourKlineCache = new ConcurrentHashMap<>();

    @Autowired
    private CoreConfig coreConfig;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private KlineMapper klineMapper;

    @Autowired
    private BeanMapping beanMapping;

    @Setter
    private MessageClosure messageClosure;

    private AtomicBoolean initCompleted = new AtomicBoolean(false);

    @PostConstruct
    public void init() {
        initLocksAndLatches();
        long currTimeSeconds = initOneMinTimer();
        initMarket(currTimeSeconds);
    }

    /**
     * add a new symbol to market service
     */
    @Override
    public void addSymbol(SymbolInfo symbolInfo) {
        if (localCache.containsMarketLock(symbolInfo.getSymbol())) {
            return;
        }
        initLockAndLatch(symbolInfo);
        initMarket(Utils.currentSecondTime(), symbolInfo);
    }

    @Override
    public void tradeHandler(Trade trade) {
        String symbol = trade.getSymbol();
        Lock lock = localCache.getMarketLock(symbol);
        lock.lock();
        try {
            updateKlineByTrade(trade);
            updateTickerByTrade(trade);

            // push ticker
            pushTicker(symbol, false);
            // push kline
            pushKline(symbol);
            // push trade
            pushTrade(trade);

        } finally {
            lock.unlock();
        }
    }

    /**
     * update kline by trade
     */
    private void updateKlineByTrade(Trade trade) {
        Map<KlinePeriod, Kline> klinePeriods = localCache.getLastKlinePeriods(trade.getSymbol());
        for (Kline kline : klinePeriods.values()) {
            if (kline.getCount() == 0) {
                kline.setOpenPrice(trade.getPrice());
                kline.setHighPrice(trade.getPrice());
                kline.setLowPrice(trade.getPrice());
            } else {
                if (trade.getPrice().compareTo(kline.getHighPrice()) > 0) {
                    kline.setHighPrice(trade.getPrice());
                }
                if (trade.getPrice().compareTo(kline.getLowPrice()) < 0) {
                    kline.setLowPrice(trade.getPrice());
                }
            }
            kline.setClosePrice(trade.getPrice());
            kline.setVolume(kline.getVolume().add(trade.getQty().multiply(BigDecimal.valueOf(2))));
            kline.setQuoteVolume(kline.getQuoteVolume().add(trade.getQuoteQty().multiply(BigDecimal.valueOf(2))));
            kline.setCount(kline.getCount() + 2);
        }
    }

    /**
     * update ticker by trade
     */
    private void updateTickerByTrade(Trade trade) {
        Ticker ticker = localCache.getLastTicker(trade.getSymbol());
        if (ticker.getCount() == 0) {
            ticker.setOpenPrice(trade.getPrice());
            ticker.setHighPrice(trade.getPrice());
            ticker.setLowPrice(trade.getPrice());
        } else {
            if (trade.getPrice().compareTo(ticker.getHighPrice()) > 0) {
                ticker.setHighPrice(trade.getPrice());
            }
            if (trade.getPrice().compareTo(ticker.getLowPrice()) < 0) {
                ticker.setLowPrice(trade.getPrice());
            }
        }
        ticker.setLastPrice(trade.getPrice());
        ticker.setLastQty(trade.getQty());
        ticker.setVolume(ticker.getVolume().add(trade.getQty().multiply(BigDecimal.valueOf(2))));
        ticker.setQuoteVolume(ticker.getQuoteVolume().add(trade.getQuoteQty().multiply(BigDecimal.valueOf(2))));
        ticker.setCount(ticker.getCount() + 2);
        ticker.refreshPriceChange();
    }

    /**
     * init locks nad initFinishFlags
     */
    private void initLocksAndLatches() {
        for (SymbolInfo symbolInfo : localCache.getAllSymbols().values()) {
            if (symbolInfo.getStatus() != SymbolStatus.CONTINUOUS) {
                continue;
            }
            initLockAndLatch(symbolInfo);
        }
    }

    /**
     * init lock and initFinishFlag
     */
    private void initLockAndLatch(SymbolInfo symbolInfo) {
        ReentrantLock lock = new ReentrantLock();
        localCache.setMarketLock(symbolInfo.getSymbol(), lock);
        initFinishLatches.put(symbolInfo.getSymbol(), new CountDownLatch(1));
    }

    /**
     * init market
     */
    private void initMarket(long currTimeSeconds) {
        localCache.getAllSymbols().values().parallelStream().forEach(symbolInfo -> {
            if (symbolInfo.getStatus() != SymbolStatus.CONTINUOUS) {
                return;
            }
            initMarket(currTimeSeconds, symbolInfo);
        });
        if (initCompleted()) {
            initCompleted.set(true);
            log.info("jtrade market initialization completed.");
        }
    }

    /**
     * init market by symbol
     */
    private void initMarket(long currTimeSeconds, SymbolInfo symbolInfo) {
        Lock lock = localCache.getMarketLock(symbolInfo.getSymbol());
        lock.lock();
        try {
            initMarketProcess(currTimeSeconds, symbolInfo);
        } finally {
            lock.unlock();
        }
        // latch countDown
        initFinishLatches.get(symbolInfo.getSymbol()).countDown();
    }

    /**
     * init market by symbol
     */
    private void initMarketProcess(long currTimeSeconds, SymbolInfo symbolInfo) {
        initKline(symbolInfo, currTimeSeconds);
        initOneMinKlineCache(symbolInfo, currTimeSeconds);
        initOneHourKlineCache(symbolInfo, currTimeSeconds);

        Ticker ticker = new Ticker(symbolInfo.getSymbol());
        localCache.setLastTicker(symbolInfo.getSymbol(), ticker);
        initTicker(symbolInfo.getSymbol(), currTimeSeconds, ticker);
    }

    /**
     * init kline
     */
    private void initKline(SymbolInfo symbolInfo, long currTimeSeconds) {
        /**
         * There are dependencies between each period and must be executed sequentially.
         */
        for (KlinePeriod period : KlinePeriod.values()) {
            // fill in missing kline data
            fillMissKlineOnPeriod(symbolInfo.getSymbol(), currTimeSeconds, period);
            // init last kline cache
            initLastKlineOnPeriod(symbolInfo, currTimeSeconds, period);
        }
    }

    /**
     * fill in missing kline data on a period
     */
    private void fillMissKlineOnPeriod(String symbol, long currTimeSeconds, KlinePeriod period) {
        String tableName = getKlineTableName(symbol.toLowerCase());
        Long oldLastTime = klineMapper.getLastKlineTime(symbol, period.getPeriod(), tableName);
        if (oldLastTime == null) {
            Long oldBaseFirstTime = klineMapper.getFirstKlineTime(symbol, period.getBasePeriod(), tableName);
            if (oldBaseFirstTime != null) {
                oldLastTime = Utils.getPeriodBeginTime(oldBaseFirstTime, period.getSeconds()) - period.getSeconds();
            }
        }

        if (oldLastTime != null) {
            Kline oldLastKline = null;
            if (period == KlinePeriod.ONE_MIN) {
                oldLastKline = klineMapper.getKline(symbol, period.getPeriod(), oldLastTime, tableName);
                oldLastKline.setOpenPrice(oldLastKline.getClosePrice());
                oldLastKline.setHighPrice(oldLastKline.getClosePrice());
                oldLastKline.setLowPrice(oldLastKline.getClosePrice());
                oldLastKline.setVolume(BigDecimal.ZERO);
                oldLastKline.setQuoteVolume(BigDecimal.ZERO);
                oldLastKline.setCount(0);
            }

            long lastBeginTime = Utils.getPeriodBeginTime(currTimeSeconds, period.getSeconds());
            long firstBeginTime = oldLastTime + period.getSeconds();
            while (firstBeginTime < lastBeginTime) {
                Kline kline;
                if (period == KlinePeriod.ONE_MIN) {
                    kline = oldLastKline;
                } else {
                    kline = klineMapper.statKline(symbol, period.getBasePeriod(), firstBeginTime,
                        firstBeginTime + period.getSeconds() - 1, tableName);
                    if (kline == null) {
                        kline = klineMapper.getKline(symbol, period.getBasePeriod(), firstBeginTime, tableName);
                    }
                }
                kline.setSymbol(symbol);
                kline.setPeriod(period.getPeriod());
                kline.setBeginTime(firstBeginTime);
                kline.setEndTime(firstBeginTime + period.getSeconds());
                kline.setTableName(tableName);
                klineMapper.addKline(kline);
                firstBeginTime += period.getSeconds();
            }
        }
    }

    /**
     * init last kline cache on a period
     */
    private void initLastKlineOnPeriod(SymbolInfo symbolInfo, long currTimeSeconds, KlinePeriod period) {
        String symbol = symbolInfo.getSymbol();
        String tableName = getKlineTableName(symbol.toLowerCase());
        Long lastBeginTime = Utils.getPeriodBeginTime(currTimeSeconds, period.getSeconds());
        Kline kline = klineMapper.statKline(symbol, KlinePeriod.ONE_MIN.getPeriod(), lastBeginTime,
            lastBeginTime + period.getSeconds(), tableName);
        if (kline == null) {
            Long oldLastTime = klineMapper.getLastKlineTime(symbol, KlinePeriod.ONE_MIN.getPeriod(), tableName);
            if (oldLastTime != null) {
                Kline oldLastKline =
                    klineMapper.getKline(symbol, KlinePeriod.ONE_MIN.getPeriod(), oldLastTime, tableName);
                oldLastKline.setOpenPrice(oldLastKline.getClosePrice());
                oldLastKline.setHighPrice(oldLastKline.getClosePrice());
                oldLastKline.setLowPrice(oldLastKline.getClosePrice());
                oldLastKline.setVolume(BigDecimal.ZERO);
                oldLastKline.setQuoteVolume(BigDecimal.ZERO);
                oldLastKline.setCount(0);
                kline = oldLastKline;
            } else {
                kline = new Kline();
            }
        }

        if (kline != null) {
            Utils.formatKline(kline, symbolInfo);
            kline.setSymbol(symbol);
            kline.setPeriod(period.getPeriod());
            kline.setBeginTime(lastBeginTime);
            kline.setEndTime(lastBeginTime + period.getSeconds());
            kline.setTableName(tableName);

            Map<KlinePeriod, Kline> klinePeriods = localCache.getLastKlinePeriods(symbol);
            klinePeriods.put(period, kline);
        }
    }

    /**
     * init oneMinKlineCache
     */
    private void initOneMinKlineCache(SymbolInfo symbolInfo, long currTimeSeconds) {
        String symbol = symbolInfo.getSymbol();
        String tableName = getKlineTableName(symbol.toLowerCase());
        long endTime = Utils.getPeriodBeginTime(currTimeSeconds, KlinePeriod.ONE_MIN.getSeconds());
        long beginTime = endTime - ONE_MIN_NUMBER * KlinePeriod.ONE_MIN.getSeconds();
        List<Kline> klineList =
            klineMapper.getKlineList(symbol, KlinePeriod.ONE_MIN.getPeriod(), beginTime, endTime - 1, tableName);
        if (klineList == null) {
            klineList = new ArrayList<>();
        }
        for (Kline kline : klineList) {
            Utils.formatKline(kline, symbolInfo);
        }
        oneMinKlineCache.put(symbol, klineList);
    }

    /**
     * init oneHourKlineCache
     */
    private void initOneHourKlineCache(SymbolInfo symbolInfo, long currTimeSeconds) {
        String symbol = symbolInfo.getSymbol();
        String tableName = getKlineTableName(symbol.toLowerCase());
        long endTime = Utils.getPeriodBeginTime(currTimeSeconds, KlinePeriod.ONE_HOUR.getSeconds());
        long beginTime = endTime - ONE_HOUR_NUMBER * KlinePeriod.ONE_HOUR.getSeconds();
        List<Kline> klineList =
            klineMapper.getKlineList(symbol, KlinePeriod.ONE_HOUR.getPeriod(), beginTime, endTime - 1, tableName);
        if (klineList == null) {
            klineList = new ArrayList<>();
        }
        for (Kline kline : klineList) {
            Utils.formatKline(kline, symbolInfo);
        }
        oneHourKlineCache.put(symbol, klineList);
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
            localCache.getAllSymbols().values().parallelStream().forEach(symbolInfo -> {
                if (symbolInfo.getStatus() != SymbolStatus.CONTINUOUS) {
                    return;
                }
                /**
                 * The waitForInitFinished needs to be placed outside the lock, otherwise it may cause a deadlock.
                 */
                waitForInitFinished(symbolInfo.getSymbol());
                Lock lock = localCache.getMarketLock(symbolInfo.getSymbol());
                lock.lock();
                try {
                    for (KlinePeriod period : KlinePeriod.values()) {
                        if (Utils.isNewPeriod(currTimeSeconds, period.getSeconds())) {
                            Kline kline = localCache.getLastKlinePeriods(symbolInfo.getSymbol()).get(period);
                            long newBeginTime = Utils.getPeriodBeginTime(currTimeSeconds, period.getSeconds());

                            if (newBeginTime < kline.getEndTime()) {
                                log.warn("{} {} kline period switching error: newBeginTime={} < lastEndTime={}",
                                    symbolInfo.getSymbol(), period.getPeriod(), newBeginTime, kline.getEndTime());

                            } else if (newBeginTime > kline.getEndTime()) {
                                log.warn("{} {} kline period switching error: newBeginTime={} > lastEndTime={}",
                                    symbolInfo.getSymbol(), period.getPeriod(), newBeginTime, kline.getEndTime());
                                // reinitialize the market using the current time
                                initMarketProcess(Utils.currentSecondTime(), symbolInfo);

                            } else {
                                long currentBeginTime =
                                    Utils.getPeriodBeginTime(Utils.currentSecondTime(), period.getSeconds());
                                if (currentBeginTime > newBeginTime) {
                                    // reinitialize the market using the current time
                                    initMarketProcess(Utils.currentSecondTime(), symbolInfo);

                                } else {
                                    if (kline.getOpenPrice().compareTo(BigDecimal.ZERO) > 0) {
                                        // save kline data to database
                                        klineMapper.addKline(kline);

                                        // update ONE_MIN、ONE_HOUR cache
                                        if (period == KlinePeriod.ONE_MIN) {
                                            updateOneMinKlineCache(symbolInfo.getSymbol(), kline);
                                        }
                                        if (period == KlinePeriod.ONE_HOUR) {
                                            updateOneHourKlineCache(symbolInfo.getSymbol(), kline);
                                        }
                                        // reinitialize ticker
                                        Ticker ticker = localCache.getLastTicker(symbolInfo.getSymbol());
                                        ticker.clear();
                                        initTicker(symbolInfo.getSymbol(), currTimeSeconds, ticker);

                                        /**
                                         * push ticker Executed in a separate thread, needs to be pushed to the public
                                         * queue.
                                         */
                                        pushTicker(symbolInfo.getSymbol(), true);

                                        /**
                                         * push period kline Executed in a separate thread, needs to be pushed to the
                                         * public queue.
                                         */
                                        pushKline(symbolInfo.getSymbol(), period, true);
                                    }

                                    // reinitialize kline
                                    kline.setBeginTime(newBeginTime);
                                    kline.setEndTime(newBeginTime + period.getSeconds());
                                    kline.setOpenPrice(kline.getClosePrice());
                                    kline.setHighPrice(kline.getClosePrice());
                                    kline.setLowPrice(kline.getClosePrice());
                                    kline.setVolume(BigDecimal.ZERO);
                                    kline.setQuoteVolume(BigDecimal.ZERO);
                                    kline.setCount(0);
                                }
                            }
                        }
                    }
                } finally {
                    lock.unlock();
                }
            });
            if (!initCompleted.get()) {
                if (initCompleted()) {
                    initCompleted.set(true);
                    log.info("jtrade market initialization completed.");
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * push ticker
     */
    private void pushTicker(String symbol, boolean toQueue) {
        if (messageClosure == null) {
            return;
        }
        Ticker ticker = localCache.getLastTicker(symbol);
        StreamArgument argument = new StreamArgument(StreamChannel.TICKER.getCode(), symbol);
        String content = StreamUtils.getJSONString(argument.toJSONString(), ticker.toJSONString());
        if (toQueue) {
            messageClosure.publishToQueue(CommandIdentity.PUBLISH_RAW, content);
        } else {
            messageClosure.publish(content, false);
        }
    }

    /**
     * push kline
     */
    private void pushKline(String symbol) {
        for (KlinePeriod period : KlinePeriod.values()) {
            pushKline(symbol, period, false);
        }
    }

    /**
     * push kline
     */
    private void pushKline(String symbol, KlinePeriod period, boolean toQueue) {
        if (messageClosure == null) {
            return;
        }
        StreamArgument argument = new StreamArgument(period.getChannelName(), symbol);
        Kline kline = localCache.getLastKlinePeriods(symbol).get(period);
        String content = StreamUtils.getJSONString(argument.toJSONString(), kline.toJSONString());
        if (toQueue) {
            messageClosure.publishToQueue(CommandIdentity.PUBLISH_RAW, content);
        } else {
            messageClosure.publish(content, false);
        }
    }

    /**
     * push trade
     */
    private void pushTrade(Trade trade) {
        StreamArgument argument = new StreamArgument(StreamChannel.TRADE.getCode(), trade.getSymbol());
        String content = StreamUtils.getJSONString(argument.toJSONString(), trade.toJSONString());
        messageClosure.publish(content, true);
    }

    /**
     * update oneMinKlineCache
     */
    private void updateOneMinKlineCache(String symbol, Kline kline) {
        List<Kline> klineList = oneMinKlineCache.get(symbol);
        if (klineList.size() >= ONE_MIN_NUMBER) {
            klineList.remove(0);
        }
        klineList.add(kline);
    }

    /**
     * update oneHourKlineCache
     */
    private void updateOneHourKlineCache(String symbol, Kline kline) {
        List<Kline> klineList = oneHourKlineCache.get(symbol);
        if (klineList.size() >= ONE_HOUR_NUMBER) {
            klineList.remove(0);
        }
        klineList.add(kline);
    }

    /**
     * initialize ticker
     */
    private void initTicker(String symbol, long currTimeSeconds, Ticker ticker) {
        List<Kline> oneMinList = oneMinKlineCache.get(symbol);
        List<Kline> oneHourList = oneHourKlineCache.get(symbol);

        long oneHourLastEndTime = Utils.getPeriodBeginTime(currTimeSeconds, KlinePeriod.ONE_HOUR.getSeconds());
        long oneHourFirstBeginTime = oneHourLastEndTime - ONE_HOUR_NUMBER * KlinePeriod.ONE_HOUR.getSeconds();
        // First
        for (Kline kline : oneMinList) {
            long klineTime = kline.getBeginTime();
            if (klineTime >= oneHourFirstBeginTime) {
                break;
            }
            updateTicker(ticker, kline);
        }
        // Middle
        for (Kline kline : oneHourList) {
            updateTicker(ticker, kline);
        }
        // Last
        for (int i = oneMinList.size() - 1; i >= 0; i--) {
            Kline kline = oneMinList.get(i);
            long klineTime = kline.getBeginTime();
            if (klineTime < oneHourLastEndTime) {
                break;
            }
            updateTicker(ticker, kline);
        }

        if (!CollectionUtils.isEmpty(oneMinList)) {
            Kline kline = oneMinList.get(oneMinList.size() - 1);
            ticker.setLastPrice(kline.getClosePrice());
            if (ticker.getCount() == 0) {
                ticker.setOpenPrice(kline.getOpenPrice());
                ticker.setHighPrice(kline.getHighPrice());
                ticker.setLowPrice(kline.getLowPrice());
            }
        }
        ticker.refreshPriceChange();
    }

    /**
     * get kline table name of the symbol
     */
    private String getKlineTableName(String symbol) {
        return Utils.format(Constants.KLINE_TABLE_NAME, coreConfig.getMarketDbSchema(),
            StringUtils.replace(symbol, coreConfig.getSymbolDelimiter(), Constants.UNDER_LINE));
    }

    /**
     * wait for init market finished
     */
    private void waitForInitFinished(String symbol) {
        try {
            initFinishLatches.get(symbol).await();
        } catch (InterruptedException e) {
            throw new TradeException(e);
        }
    }

    /**
     * update ticker
     */
    private void updateTicker(Ticker ticker, Kline kline) {
        if (kline.getCount() > 0) {
            if (ticker.getCount() == 0) {
                ticker.setOpenPrice(kline.getOpenPrice());
                ticker.setHighPrice(kline.getHighPrice());
                ticker.setLowPrice(kline.getLowPrice());
            } else {
                if (kline.getHighPrice().compareTo(ticker.getHighPrice()) > 0) {
                    ticker.setHighPrice(kline.getHighPrice());
                }
                if (kline.getLowPrice().compareTo(ticker.getLowPrice()) < 0) {
                    ticker.setLowPrice(kline.getLowPrice());
                }
            }
            ticker.setVolume(ticker.getVolume().add(kline.getVolume()));
            ticker.setQuoteVolume(ticker.getQuoteVolume().add(kline.getQuoteVolume()));
            ticker.setCount(ticker.getCount() + kline.getCount());
        }
    }

    /**
     * check if initialization is complete
     */
    private boolean initCompleted() {
        boolean completed = true;
        for (SymbolInfo symbolInfo : localCache.getAllSymbols().values()) {
            if (symbolInfo.getStatus() != SymbolStatus.CONTINUOUS) {
                continue;
            }
            Kline kline = localCache.getLastKlinePeriods(symbolInfo.getSymbol()).get(KlinePeriod.ONE_MIN);
            long currentBeginTime =
                Utils.getPeriodBeginTime(Utils.currentSecondTime(), KlinePeriod.ONE_MIN.getSeconds());
            if (currentBeginTime != kline.getBeginTime()) {
                completed = false;
                break;
            }
        }
        return completed;
    }

}
