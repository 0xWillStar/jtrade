package com.crypto.jtrade.core.provider.service.match.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.OrderType;
import com.crypto.jtrade.common.constants.SymbolStatus;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.common.util.TimerManager;
import com.crypto.jtrade.core.provider.config.CoreConfig;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.cache.RedisService;
import com.crypto.jtrade.core.provider.service.match.MatchEngine;
import com.crypto.jtrade.core.provider.service.match.MatchEngineManager;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * match engine manager
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class MatchEngineManagerImpl implements ApplicationContextAware, MatchEngineManager {

    private int engineIndex;

    @Setter
    private ApplicationContext applicationContext;

    @Autowired
    private CoreConfig coreConfig;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private RedisService redisService;

    /**
     * thread factory for disruptor
     */
    private NamedThreadFactory threadFactory = new NamedThreadFactory("jtrade-match-disruptor-", true);

    /**
     * KEY：symbol
     */
    private ConcurrentHashMap<String, MatchEngine> matchEngines = new ConcurrentHashMap<>();

    private List<MatchEngine> sharedEngineList = new ArrayList<>();

    @PostConstruct
    public void init() {
        initMatchEngine();
        initPublishDepthTimer();
        loadOldOpenOrders();
    }

    /**
     * init match engine
     */
    private void initMatchEngine() {
        Collection<SymbolInfo> allSymbols = localCache.getAllSymbols().values();
        int independents = (int)allSymbols.stream().filter(SymbolInfo::getIndependentMatchThread).count();
        int minExpects = Math.min(1, allSymbols.size() - independents) + independents;
        if (coreConfig.getMatchEngineSize() < minExpects) {
            log.error("The expected minimum number of match engine is {}, but the config is {}", minExpects,
                coreConfig.getMatchEngineSize());
            throw new TradeException(TradeError.MATCH_ENGINE_INSUFFICIENT);
        }

        int size = allSymbols.size() - independents;
        int count = Math.min(size, coreConfig.getMatchEngineSize() - independents);
        int number = 0;
        int left = 0;
        if (count > 0) {
            number = size / count;
            left = size % count;
        }

        engineIndex = 1;
        int index = 1;
        List<SymbolInfo> symbolList = new ArrayList<>();
        for (SymbolInfo symbolInfo : allSymbols) {
            if (symbolInfo.getStatus() != SymbolStatus.CONTINUOUS) {
                continue;
            }
            if (symbolInfo.getIndependentMatchThread()) {
                MatchEngine engine = applicationContext.getBean(MatchEngine.class);
                engine.init(engineIndex++, Arrays.asList(symbolInfo), threadFactory);
                matchEngines.put(symbolInfo.getSymbol(), engine);

            } else if (count > 0) {
                symbolList.add(symbolInfo);
                if ((index < number) || (index == number && left > 0)) {
                    index++;
                    continue;
                } else {
                    MatchEngine engine = applicationContext.getBean(MatchEngine.class);
                    engine.init(engineIndex++, symbolList, threadFactory);
                    sharedEngineList.add(engine);
                    for (SymbolInfo symbol : symbolList) {
                        matchEngines.put(symbol.getSymbol(), engine);
                    }

                    index = 1;
                    left--;
                    symbolList = new ArrayList<>();
                }
            }
        }
    }

    /**
     * add a new symbol to engine
     */
    @Override
    public void addSymbol(SymbolInfo symbolInfo) {
        if (matchEngines.containsKey(symbolInfo.getSymbol())) {
            return;
        }

        if (symbolInfo.getIndependentMatchThread()) {
            MatchEngine engine = applicationContext.getBean(MatchEngine.class);
            engine.init(engineIndex++, Arrays.asList(symbolInfo), threadFactory);
            matchEngines.put(symbolInfo.getSymbol(), engine);

        } else {
            if (sharedEngineList.size() == 0) {
                MatchEngine engine = applicationContext.getBean(MatchEngine.class);
                engine.init(engineIndex++, Arrays.asList(symbolInfo), threadFactory);
                matchEngines.put(symbolInfo.getSymbol(), engine);
                sharedEngineList.add(engine);
            } else {
                sharedEngineList.sort((e1, e2) -> {
                    if (e1.getSymbols().size() > e2.getSymbols().size()) {
                        return 1;
                    } else if (e1.getSymbols().size() < e2.getSymbols().size()) {
                        return -1;
                    } else {
                        return 0;
                    }
                });
                sharedEngineList.get(0).addSymbol(symbolInfo);
            }
        }
    }

    /**
     * get match engine by the symbol
     */
    @Override
    public MatchEngine getMatchEngine(String symbol) {
        MatchEngine engine = matchEngines.get(symbol);
        if (engine == null) {
            throw new TradeException(TradeError.MATCH_ENGINE_NOT_EXIST);
        } else {
            return engine;
        }
    }

    /**
     * load old open orders
     */
    private void loadOldOpenOrders() {
        List<Order> orderList = new ArrayList<>();
        List<Object> ordersList = redisService.getAllOpenOrders();
        for (Object orders : ordersList) {
            Map<Object, Object> orderMap = (Map<Object, Object>)orders;
            for (Object orderStr : orderMap.values()) {
                Order order = Order.toObject((String)orderStr);
                orderList.add(order);
            }
        }
        // Orders are sorted ascend by the orderId
        orderList.sort(Comparator.comparing(o -> Long.valueOf(o.getOrderId())));
        for (Order order : orderList) {
            if (order.getType() == OrderType.LIMIT || order.getType() == OrderType.MARKET) {
                MatchEngine matchEngine = getMatchEngine(order.getSymbol());
                matchEngine.loadOrder(order);
            } else if (order.getType() == OrderType.STOP || order.getType() == OrderType.STOP_MARKET
                || order.getType() == OrderType.TAKE_PROFIT || order.getType() == OrderType.TAKE_PROFIT_MARKET) {
                localCache.getStopOrderCache(order.getSymbol()).addOrder(order);
            }
        }
    }

    /**
     * init publish depth timer
     */
    private void initPublishDepthTimer() {
        long currTime = System.currentTimeMillis();
        int interval = coreConfig.getDepthIntervalMilliSeconds();
        long delay = (currTime / interval + 1) * interval - currTime;
        TimerManager.scheduleAtFixedRate(() -> onTimePublishDepth(), delay, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * time to publish depth
     */
    private void onTimePublishDepth() {
        try {
            for (String symbol : matchEngines.keySet()) {
                MatchEngine matchEngine = getMatchEngine(symbol);
                matchEngine.publishDepthEvent();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
