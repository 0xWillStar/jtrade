package com.crypto.jtrade.core.provider.service.trade.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.crypto.jtrade.common.constants.MatchRole;
import com.crypto.jtrade.common.constants.SymbolStatus;
import com.crypto.jtrade.common.constants.TradeType;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.SymbolIndicator;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.util.TimerManager;
import com.crypto.jtrade.common.util.Utils;
import com.crypto.jtrade.core.api.model.CancelOrderRequest;
import com.crypto.jtrade.core.api.model.EmptyRequest;
import com.crypto.jtrade.core.api.model.OTCRequest;
import com.crypto.jtrade.core.provider.model.landing.SymbolInfoLanding;
import com.crypto.jtrade.core.provider.service.cache.ClientEntity;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.landing.RedisLanding;
import com.crypto.jtrade.core.provider.service.match.MatchEngineManager;
import com.crypto.jtrade.core.provider.service.publish.MarketService;
import com.crypto.jtrade.core.provider.service.trade.SymbolService;
import com.crypto.jtrade.core.provider.service.trade.TradeService;

import lombok.extern.slf4j.Slf4j;

/**
 * symbol service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class SymbolServiceImpl implements SymbolService {

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private MatchEngineManager matchEngineManager;

    @Autowired
    private MarketService marketService;

    @Autowired
    private RedisLanding redisLanding;

    @Autowired
    private TradeService tradeService;

    @PostConstruct
    public void init() {
        initOneMinTimer();
    }

    /**
     * init one minute timer
     */
    private void initOneMinTimer() {
        long currTimeSeconds = Utils.currentSecondTime();
        int interval = 60;
        long delay = (currTimeSeconds / interval + 1) * interval - currTimeSeconds;
        TimerManager.scheduleAtFixedRate(() -> onTimeOneMin(), delay, interval, TimeUnit.SECONDS);
    }

    /**
     * time to one minute, check if the symbol status needs to be updated.
     */
    private void onTimeOneMin() {
        try {
            long currTimeSeconds = Utils.currentSecondTime();
            ReentrantLock lock = localCache.getSymbolWriteLock();
            lock.lock();
            try {
                /**
                 * cannot be parallelized, each symbol needs to be processed serially.
                 */
                for (SymbolInfo symbolInfo : localCache.getAllSymbols().values()) {
                    boolean updated = false;
                    if (symbolInfo.getStatus() == SymbolStatus.NO_TRADING) {
                        if (symbolInfo.getOrderingTime() != null) {
                            if (symbolInfo.getOrderingTime() <= currTimeSeconds) {
                                symbolInfo.setStatus(SymbolStatus.AUCTION_ORDERING);
                                updated = true;
                            }
                        } else if (symbolInfo.getTradingTime() != null
                            && symbolInfo.getTradingTime() <= currTimeSeconds) {
                            statusToContinuous(symbolInfo);
                            updated = true;
                        }
                    } else if (symbolInfo.getStatus() == SymbolStatus.AUCTION_ORDERING) {
                        if (symbolInfo.getTradingTime() != null && symbolInfo.getTradingTime() <= currTimeSeconds) {
                            statusToContinuous(symbolInfo);
                            updated = true;
                        }
                    } else if (symbolInfo.getStatus() == SymbolStatus.CONTINUOUS) {
                        if (symbolInfo.getExpireTime() != null && symbolInfo.getExpireTime() <= currTimeSeconds) {
                            statusToNotActive(symbolInfo);
                            updated = true;
                        }
                    }
                    if (updated) {
                        SymbolInfoLanding landing = new SymbolInfoLanding(null, symbolInfo);
                        redisLanding.setSymbolInfo(landing);
                    }
                }
            } finally {
                lock.unlock();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * the symbol status updates to CONTINUOUS
     */
    private void statusToContinuous(SymbolInfo symbolInfo) {
        matchEngineManager.addSymbol(symbolInfo);
        marketService.addSymbol(symbolInfo);
        /**
         * the symbol can be traded now
         */
        symbolInfo.setStatus(SymbolStatus.CONTINUOUS);
    }

    /**
     * the symbol status updates to NOT_ACTIVE
     */
    private void statusToNotActive(SymbolInfo symbolInfo) {
        /**
         * the symbol can't be traded now
         */
        symbolInfo.setStatus(SymbolStatus.NOT_ACTIVE);
        /**
         * synchronous request, receiving the response indicating that all requests before the symbol status updates to
         * NOT_ACTIVE have been processed.
         */
        if (!sendEmptyCommandSync(symbolInfo.getSymbol())) {
            return;
        }

        cancelOrders(symbolInfo.getSymbol());
        BigDecimal markPrice = getMarkPrice(symbolInfo.getSymbol());
        closePositions(symbolInfo.getSymbol(), markPrice);
    }

    /**
     * send empty command synchronous
     */
    private boolean sendEmptyCommandSync(String symbol) {
        CountDownLatch latch = new CountDownLatch(1);
        BaseResponse response = tradeService.emptyCommand(new EmptyRequest(symbol, latch));
        if (response.isError()) {
            log.error("send empty command is fail during updating symbol status to NOT_ACTIVE: {}, {}", symbol,
                response.getMsg());
            return false;
        } else {
            try {
                latch.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("send empty command is timeout during updating symbol status to NOT_ACTIVE: {}", symbol);
                return false;
            }
        }
        return true;
    }

    /**
     * cancel orders
     */
    private void cancelOrders(String symbol) {
        Iterator<String> iterator = localCache.getOrderClientIds().iterator();
        while (iterator.hasNext()) {
            String clientId = iterator.next();
            ClientEntity clientEntity = localCache.getClientEntity(clientId);
            for (Order order : clientEntity.getOrders().values()) {
                if (order.getSymbol().equals(symbol)) {
                    CancelOrderRequest request = new CancelOrderRequest();
                    request.setClientId(clientId);
                    request.setSymbol(symbol);
                    request.setOrderId(order.getOrderId());
                    tradeService.cancelOrder(request);
                }
            }
        }
    }

    /**
     * close positions
     */
    private void closePositions(String symbol, BigDecimal markPrice) {
        List<Position> longPositions = new ArrayList<>();
        List<Position> shortPositions = new ArrayList<>();
        if (prepareClosePosition(symbol, longPositions, shortPositions)) {
            for (Position longPosition : longPositions) {
                BigDecimal longAmt = longPosition.getPositionAmt();

                Iterator<Position> iterator = shortPositions.iterator();
                while (iterator.hasNext()) {
                    Position shortPosition = iterator.next();
                    BigDecimal shortAmt = shortPosition.getPositionAmt().abs();

                    BigDecimal closeAmt = BigDecimal.ZERO;
                    if (longAmt.compareTo(shortAmt) >= 0) {
                        closeAmt = shortAmt;
                        longAmt = longAmt.subtract(shortAmt);
                        shortAmt = BigDecimal.ZERO;
                    } else {
                        closeAmt = longAmt;
                        shortAmt = shortAmt.subtract(longAmt);
                        longAmt = BigDecimal.ZERO;
                    }

                    /**
                     * closePosition is synchronous, after executed the positionAmt is updated.
                     */
                    closePosition(symbol, markPrice, closeAmt, shortPosition.getClientId(), longPosition.getClientId());
                    if (shortAmt.compareTo(BigDecimal.ZERO) == 0) {
                        iterator.remove();
                    } else if (longAmt.compareTo(BigDecimal.ZERO) == 0) {
                        break;
                    }
                }
            }
        }
    }

    /**
     * Prepare to close the position
     */
    private boolean prepareClosePosition(String symbol, List<Position> longPositions, List<Position> shortPositions) {
        Iterator<String> iterator = localCache.getPositionClientIds().iterator();
        while (iterator.hasNext()) {
            String clientId = iterator.next();
            ClientEntity clientEntity = localCache.getClientEntity(clientId);
            Position position = clientEntity.getPositions().get(symbol);
            if (position != null) {
                if (position.getPositionAmt().compareTo(BigDecimal.ZERO) > 0) {
                    longPositions.add(position);
                } else if (position.getPositionAmt().compareTo(BigDecimal.ZERO) < 0) {
                    shortPositions.add(position);
                }
            }
        }
        BigDecimal longAmount = getPositionAmount(longPositions);
        BigDecimal shortAmount = getPositionAmount(shortPositions);
        if (longAmount.compareTo(shortAmount.abs()) != 0) {
            log.error("long and short positions are not equal, symbol: {}, long: {}, short: {}", symbol, longAmount,
                shortAmount);
            return false;
        }
        /**
         * sort the positions, LONG: ascend, SHORT: descend
         */
        longPositions.sort(Comparator.comparing(Position::getPositionAmt));
        shortPositions.sort((o1, o2) -> o2.getPositionAmt().compareTo(o1.getPositionAmt()));
        return true;
    }

    /**
     * close position
     */
    private void closePosition(String symbol, BigDecimal price, BigDecimal quantity, String buyClientId,
        String sellClientId) {
        OTCRequest otcRequest = new OTCRequest();
        otcRequest.setClientId1(buyClientId);
        otcRequest.setClientId2(sellClientId);

        OTCRequest.Detail detail = new OTCRequest.Detail();
        detail.setSymbol(symbol);
        detail.setPrice(price);
        detail.setQuantity(quantity);
        detail.setTradeType(TradeType.LIQUIDATION);
        detail.setBuyClientId(buyClientId);
        detail.setBuyMatchRole(MatchRole.MAKER);
        detail.setSellClientId(sellClientId);
        detail.setSellMatchRole(MatchRole.TAKER);
        List<OTCRequest.Detail> detailList = new ArrayList<>();
        detailList.add(detail);
        otcRequest.setDetailList(detailList);
        BaseResponse response = tradeService.otcTrade(otcRequest);
        if (response.isError()) {
            log.error("close position is fail during updating symbol status to NOT_ACTIVE: {}, {}",
                JSON.toJSONString(otcRequest), response.getMsg());
        } else {
            log.info("close position is success during updating symbol status to NOT_ACTIVE: {}",
                JSON.toJSONString(otcRequest));
        }
    }

    /**
     * get the sum of the position amount
     */
    private BigDecimal getPositionAmount(List<Position> positions) {
        BigDecimal total = BigDecimal.ZERO;
        for (Position position : positions) {
            total = total.add(position.getPositionAmt());
        }
        return total;
    }

    /**
     * get mark price
     */
    private BigDecimal getMarkPrice(String symbol) {
        BigDecimal markPrice = null;
        SymbolIndicator indicator = localCache.getSymbolIndicator(symbol);
        if (indicator != null) {
            markPrice = indicator.getMarkPrice();
        }
        if (markPrice == null) {
            throw new TradeException(TradeError.MARK_PRICE_NOT_EXIST);
        }
        return markPrice;
    }

}
