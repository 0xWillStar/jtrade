package com.crypto.jtrade.core.provider.service.risk.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.crypto.jtrade.common.constants.*;
import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.model.*;
import com.crypto.jtrade.common.util.BigDecimalUtil;
import com.crypto.jtrade.common.util.TimerManager;
import com.crypto.jtrade.common.util.Utils;
import com.crypto.jtrade.core.api.model.*;
import com.crypto.jtrade.core.provider.service.cache.ClientEntity;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.risk.LiquidationService;
import com.crypto.jtrade.core.provider.service.trade.TradeCommand;
import com.crypto.jtrade.core.provider.util.ClientLockHelper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * liquidation service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
@ConditionalOnProperty(value = "jtrade.liquidation.enabled")
public class LiquidationServiceImpl implements LiquidationService {

    @Value("${jtrade.liquidation.calculate-interval-seconds:5}")
    private int calculateIntervalSeconds;

    @Value("${jtrade.liquidation.delay-intervals:1}")
    private int delayIntervals;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private TradeCommand tradeCommand;

    @PostConstruct
    public void init() {
        initCalculateTimer();
    }

    /**
     * init calculate timer
     */
    private long initCalculateTimer() {
        long currTimeSeconds = Utils.currentSecondTime();
        long delay = (currTimeSeconds / calculateIntervalSeconds + 1 + delayIntervals) * calculateIntervalSeconds
            - currTimeSeconds;
        TimerManager.scheduleAtFixedRate(() -> onTimeCalculate(), delay, calculateIntervalSeconds, TimeUnit.SECONDS);
        return currTimeSeconds;
    }

    /**
     * time on liquidation
     */
    private void onTimeCalculate() {
        try {
            String insuranceClientId = localCache.getSystemParameter(SystemParameter.INSURANCE_CLIENT_ID);
            // cancel the close position order of the insurance
            insuranceCancelOrder(insuranceClientId);

            for (String clientId : localCache.getPositionClientIds()) {
                if (!clientId.equals(insuranceClientId)) {
                    ClientEntity clientEntity = localCache.getClientEntity(clientId);
                    /**
                     * cross position liquidation
                     */
                    crossLiquidation(clientEntity, insuranceClientId);
                    /**
                     * isolated position liquidation
                     */
                    for (Position position : clientEntity.getPositions().values()) {
                        if (position.getMarginType() == MarginType.ISOLATED
                            && position.getPositionAmt().compareTo(BigDecimal.ZERO) != 0) {
                            isolatedLiquidation(clientEntity, position, insuranceClientId);
                        }
                    }
                }
            }

            // All clients who need liquidation have their positions taken over, insurance close position.
            insuranceClosePosition(insuranceClientId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * cross liquidation
     */
    private void crossLiquidation(ClientEntity clientEntity, String insuranceClientId) {
        String clientId = clientEntity.getClientId();
        /**
         * check cross position whether to trigger liquidation
         */
        LiquidationSession session = checkCrossLiquidation(clientEntity);
        if (session == null) {
            /**
             * liquidation isn't triggered, check the trade authority. If the trade authority is off, it needs to be
             * turned on.
             */
            if (clientEntity.getTradeAuthority() == Constants.NO_TRADE_AUTHORITY) {
                turnOnClientTradeAuthority(clientId);
            }

            /**
             * liquidation isn't triggered, check deduct collateral assets.
             */
            if (checkDeductNoLiquidation(clientId)) {
                tradeCommand.deductCollateralAssets(clientId);
            }

        } else {
            /**
             * 0. recheck with lock
             */
            Long lastTradeId = clientEntity.getLastTradeId();
            session = checkCrossLiquidationWithLock(clientEntity);
            if (session == null) {
                return;
            }

            /**
             * 1. turn off the trade authority
             */
            turnOffClientTradeAuthority(clientId);
            /**
             * 2. cancel cross order
             */
            boolean result = cancelCrossOrderByClient(clientEntity, lastTradeId);
            if (result) {
                /**
                 * 3. take over cross position
                 */
                result = takeOverCrossByClient(clientId, session.getPositions(), session.getTotalMaintenanceMargin(),
                    session.getTotalUnrealizedProfit(), session.getBalance(), session.getFeeRate(), lastTradeId,
                    insuranceClientId);
            }

            if (result) {
                /**
                 * 4. liquidation is success, turn on the trade authority.
                 */
                turnOnClientTradeAuthority(clientId);

                /**
                 * 5. after liquidation, check deduct collateral assets.
                 */
                if (checkDeductAfterLiquidation(clientId)) {
                    tradeCommand.deductCollateralAssets(clientId);
                }
            } else {
                /**
                 * When cancel order is fail or take over is fail, if there is no position, turn on the trade authority.
                 */
                if (!localCache.getPositionClientIds().contains(clientId)) {
                    turnOnClientTradeAuthority(clientId);
                }
            }
        }
    }

    /**
     * isolated liquidation
     */
    private void isolatedLiquidation(ClientEntity clientEntity, Position position, String insuranceClientId) {
        String clientId = clientEntity.getClientId();
        if (checkIsolatedLiquidation(clientEntity, position)) {
            /**
             * 0. recheck with lock
             */
            Long lastTradeId = clientEntity.getLastTradeId();
            if (!checkIsolatedLiquidationWithLock(clientEntity, position)) {
                return;
            }

            /**
             * 1. turn off the trade authority
             */
            turnOffClientTradeAuthority(clientId);
            /**
             * 2. cancel isolated order
             */
            boolean result = cancelIsolatedOrderByClient(clientEntity, position.getSymbol(), lastTradeId);
            if (result) {
                /**
                 * 3. take over isolated position
                 */
                result = takeOverIsolatedByClient(clientEntity, position, lastTradeId, insuranceClientId);
            }

            if (result) {
                /**
                 * 4. liquidation is success, turn on the trade authority.
                 */
                turnOnClientTradeAuthority(clientId);
            } else {
                /**
                 * When cancel order is fail or take over is fail, if there is no position, turn on the trade authority.
                 */
                if (!localCache.getPositionClientIds().contains(clientId)) {
                    turnOnClientTradeAuthority(clientId);
                }
            }
        }
    }

    /**
     * check whether to trigger liquidation with lock
     */
    private LiquidationSession checkCrossLiquidationWithLock(ClientEntity clientEntity) {
        Lock lock = ClientLockHelper.getLock(clientEntity.getClientId());
        lock.lock();
        try {
            return checkCrossLiquidation(clientEntity);
        } finally {
            lock.unlock();
        }
    }

    /**
     * check whether to trigger liquidation
     */
    private LiquidationSession checkCrossLiquidation(ClientEntity clientEntity) {
        Collection<Position> positions = clientEntity.getPositions().values();
        if (CollectionUtils.isEmpty(positions)) {
            return null;
        }

        boolean triggered = false;
        Tuple3<BigDecimal, BigDecimal, BigDecimal> total = getCrossFunds(positions, clientEntity);
        if (total == null) {
            return null;
        }
        BigDecimal totalMaintenanceMargin = total.getT1();
        BigDecimal totalUnrealizedProfit = total.getT2();
        BigDecimal clearAssetBalance = total.getT3();
        if (totalMaintenanceMargin.compareTo(BigDecimal.ZERO) > 0) {
            for (Position position : positions) {
                if (position.getMarginType() == MarginType.CROSSED
                    && position.getPositionAmt().compareTo(BigDecimal.ZERO) != 0) {
                    SymbolInfo symbolInfo = localCache.getSymbolInfo(position.getSymbol());
                    BigDecimal otherMaintenanceMargin =
                        totalMaintenanceMargin.subtract(position.getMaintenanceMargin());
                    BigDecimal otherUnrealizedProfit = totalUnrealizedProfit.subtract(position.getUnrealizedProfit());
                    BigDecimal dir = position.getPositionAmt().compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.ONE
                        : BigDecimal.ONE.negate();

                    BigDecimal numerator = position.getPositionAmt().multiply(position.getOpenPrice())
                        .multiply(symbolInfo.getVolumeMultiple()).subtract(clearAssetBalance)
                        .add(otherMaintenanceMargin).subtract(otherUnrealizedProfit);
                    BigDecimal denominator = position.getPositionAmt()
                        .multiply(BigDecimal.ONE.subtract(dir.multiply(symbolInfo.getMaintenanceMarginRate())))
                        .multiply(symbolInfo.getVolumeMultiple());
                    BigDecimal liquidationPrice = BigDecimalUtil.getVal(BigDecimalUtil.divide(numerator, denominator),
                        symbolInfo.getPriceAssetScale());
                    log.info("last liquidation price: {}, {}, {}", liquidationPrice, position.getSymbol(),
                        clientEntity.getClientId());

                    if (liquidationPrice.compareTo(BigDecimal.ZERO) < 0) {
                        liquidationPrice = BigDecimal.ZERO;
                    }
                    if (position.getPositionAmt().compareTo(BigDecimal.ZERO) > 0) {
                        if (position.getMarkPrice().compareTo(liquidationPrice) <= 0) {
                            triggered = true;
                        }
                    } else {
                        if (position.getMarkPrice().compareTo(liquidationPrice) >= 0) {
                            triggered = true;
                        }
                    }
                    if (triggered) {
                        break;
                    }
                }
            }
        }
        if (triggered) {
            return new LiquidationSession(positions, totalMaintenanceMargin, totalUnrealizedProfit, clearAssetBalance,
                clientEntity.getFeeRate().getTaker(), clientEntity.getLastTradeId());
        } else {
            return null;
        }
    }

    /**
     * cancel cross order
     */
    private boolean cancelCrossOrderByClient(ClientEntity clientEntity, Long tradeId) {
        Collection<Order> orders = clientEntity.getOrders().values();
        for (Order order : orders) {
            if (order.getMarginType() == MarginType.CROSSED) {
                LiquidationCancelOrderRequest request = new LiquidationCancelOrderRequest();
                request.setExpectedTradeId(tradeId);
                CountDownLatch latch = new CountDownLatch(1);
                request.setLatch(latch);

                request.setClientId(clientEntity.getClientId());
                request.setSymbol(order.getSymbol());
                request.setOrderId(order.getOrderId());
                BaseResponse response = tradeCommand.liquidationCancelOrder(request);
                if (response.isError()) {
                    log.error("cancel order is fail during liquidation: {}, {}", JSON.toJSONString(request),
                        response.getMsg());
                    return false;
                } else {
                    try {
                        latch.await(500, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        log.error("cancel order is timeout during liquidation: {}", JSON.toJSONString(request));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * take over the cross position As long as one contract is liquidated, all contracts will be taken over.
     */
    private boolean takeOverCrossByClient(String clientId, Collection<Position> positions,
        BigDecimal totalMaintenanceMargin, BigDecimal totalUnrealizedProfit, BigDecimal balance, BigDecimal feeRate,
        Long tradeId, String insuranceClientId) {
        OTCRequest otcRequest = new OTCRequest();
        otcRequest.setClientId1(insuranceClientId);
        otcRequest.setClientId2(clientId);
        otcRequest.setExpectedTradeId2(tradeId);
        List<OTCRequest.Detail> detailList = new ArrayList<>();
        otcRequest.setDetailList(detailList);

        for (Position position : positions) {
            if (position.getMarginType() == MarginType.CROSSED
                && position.getPositionAmt().compareTo(BigDecimal.ZERO) != 0) {
                SymbolInfo symbolInfo = localCache.getSymbolInfo(position.getSymbol());
                int roundingMode;
                BigDecimal dir;
                if (position.getPositionAmt().compareTo(BigDecimal.ZERO) > 0) {
                    roundingMode = BigDecimal.ROUND_UP;
                    dir = BigDecimal.ONE;
                } else {
                    roundingMode = BigDecimal.ROUND_DOWN;
                    dir = BigDecimal.ONE.negate();
                }

                BigDecimal numerator = position.getMarkPrice()
                    .subtract(BigDecimalUtil.divide(
                        (balance.add(totalUnrealizedProfit)).multiply(
                            dir.multiply(symbolInfo.getMaintenanceMarginRate()).multiply(position.getMarkPrice())),
                        totalMaintenanceMargin));
                BigDecimal denominator = BigDecimal.ONE.subtract(dir.multiply(feeRate));
                BigDecimal takeOverPrice = BigDecimalUtil.getValEx(BigDecimalUtil.divide(numerator, denominator),
                    symbolInfo.getPriceAssetScale(), roundingMode);

                OTCRequest.Detail detail = new OTCRequest.Detail();
                detail.setSymbol(position.getSymbol());
                detail.setPrice(takeOverPrice);
                detail.setQuantity(position.getPositionAmt().abs());
                detail.setTradeType(TradeType.LIQUIDATION);
                if (position.getPositionAmt().compareTo(BigDecimal.ZERO) > 0) {
                    detail.setBuyClientId(insuranceClientId);
                    detail.setBuyMatchRole(MatchRole.TAKER);
                    detail.setSellClientId(clientId);
                    detail.setSellMatchRole(MatchRole.MAKER);
                } else {
                    detail.setBuyClientId(clientId);
                    detail.setBuyMatchRole(MatchRole.MAKER);
                    detail.setSellClientId(insuranceClientId);
                    detail.setSellMatchRole(MatchRole.TAKER);
                }
                detailList.add(detail);
            }
        }
        if (!CollectionUtils.isEmpty(detailList)) {
            BaseResponse response = tradeCommand.otcTrade(otcRequest);
            if (response.isError()) {
                log.error("take over position is fail during liquidation: {}, {}", JSON.toJSONString(otcRequest),
                    response.getMsg());
                return false;
            } else {
                log.info("take over position is success during liquidation: {}", JSON.toJSONString(otcRequest));
            }
        }
        return true;
    }

    /**
     * check whether to trigger liquidation with lock
     */
    private boolean checkIsolatedLiquidationWithLock(ClientEntity clientEntity, Position position) {
        Lock lock = ClientLockHelper.getLock(clientEntity.getClientId());
        lock.lock();
        try {
            return checkIsolatedLiquidation(clientEntity, position);
        } finally {
            lock.unlock();
        }
    }

    /**
     * check whether to trigger liquidation
     */
    private boolean checkIsolatedLiquidation(ClientEntity clientEntity, Position position) {
        boolean triggered = false;
        SymbolInfo symbolInfo = localCache.getSymbolInfo(position.getSymbol());
        BigDecimal markPrice = localCache.getSymbolIndicator(position.getSymbol()).getMarkPrice();
        if (markPrice == null) {
            log.error("the mark price of {} is null", position.getSymbol());
            return false;
        } else {
            BigDecimal dir =
                position.getPositionAmt().compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.ONE : BigDecimal.ONE.negate();
            BigDecimal numerator = position.getPositionAmt().multiply(position.getOpenPrice())
                .multiply(symbolInfo.getVolumeMultiple()).subtract(position.getIsolatedBalance());
            BigDecimal denominator = position.getPositionAmt()
                .multiply(BigDecimal.ONE.subtract(dir.multiply(symbolInfo.getMaintenanceMarginRate())))
                .multiply(symbolInfo.getVolumeMultiple());

            BigDecimal liquidationPrice =
                BigDecimalUtil.getVal(BigDecimalUtil.divide(numerator, denominator), symbolInfo.getPriceAssetScale());
            log.info("last liquidation price: {}, {}, {}", liquidationPrice, position.getSymbol(),
                clientEntity.getClientId());

            if (liquidationPrice.compareTo(BigDecimal.ZERO) < 0) {
                liquidationPrice = BigDecimal.ZERO;
            }
            if (position.getPositionAmt().compareTo(BigDecimal.ZERO) > 0) {
                if (markPrice.compareTo(liquidationPrice) <= 0) {
                    triggered = true;
                }
            } else {
                if (markPrice.compareTo(liquidationPrice) >= 0) {
                    triggered = true;
                }
            }
            position.setMarkPrice(markPrice);
        }
        return triggered;
    }

    /**
     * cancel isolated order
     */
    private boolean cancelIsolatedOrderByClient(ClientEntity clientEntity, String symbol, Long tradeId) {
        Collection<Order> orders = clientEntity.getOrders().values();
        for (Order order : orders) {
            if (order.getMarginType() == MarginType.ISOLATED && order.getSymbol().equals(symbol)) {
                LiquidationCancelOrderRequest request = new LiquidationCancelOrderRequest();
                request.setExpectedTradeId(tradeId);
                CountDownLatch latch = new CountDownLatch(1);
                request.setLatch(latch);

                request.setClientId(clientEntity.getClientId());
                request.setSymbol(order.getSymbol());
                request.setOrderId(order.getOrderId());
                BaseResponse response = tradeCommand.liquidationCancelOrder(request);
                if (response.isError()) {
                    log.error("cancel order is fail during liquidation: {}, {}", JSON.toJSONString(request),
                        response.getMsg());
                    return false;
                } else {
                    try {
                        latch.await(500, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        log.error("cancel order is timeout during liquidation: {}", JSON.toJSONString(request));
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean takeOverIsolatedByClient(ClientEntity clientEntity, Position position, Long tradeId,
        String insuranceClientId) {
        OTCRequest otcRequest = new OTCRequest();
        otcRequest.setClientId1(insuranceClientId);
        otcRequest.setClientId2(clientEntity.getClientId());
        otcRequest.setExpectedTradeId2(tradeId);
        List<OTCRequest.Detail> detailList = new ArrayList<>();
        otcRequest.setDetailList(detailList);

        if (position.getMarginType() == MarginType.ISOLATED
            && position.getPositionAmt().compareTo(BigDecimal.ZERO) != 0) {
            SymbolInfo symbolInfo = localCache.getSymbolInfo(position.getSymbol());
            int roundingMode;
            BigDecimal dir;
            if (position.getPositionAmt().compareTo(BigDecimal.ZERO) > 0) {
                roundingMode = BigDecimal.ROUND_UP;
                dir = BigDecimal.ONE;
            } else {
                roundingMode = BigDecimal.ROUND_DOWN;
                dir = BigDecimal.ONE.negate();
            }

            BigDecimal unrealizedProfit = (position.getMarkPrice().subtract(position.getOpenPrice()))
                .multiply(position.getPositionAmt()).multiply(symbolInfo.getVolumeMultiple());

            BigDecimal numerator = position.getMarkPrice().subtract(BigDecimalUtil
                .divide((position.getIsolatedBalance().add(unrealizedProfit)), position.getPositionAmt()));
            BigDecimal denominator = BigDecimal.ONE.subtract(dir.multiply(clientEntity.getFeeRate().getTaker()));
            BigDecimal takeOverPrice = BigDecimalUtil.getValEx(BigDecimalUtil.divide(numerator, denominator),
                symbolInfo.getPriceAssetScale(), roundingMode);

            OTCRequest.Detail detail = new OTCRequest.Detail();
            detail.setSymbol(position.getSymbol());
            detail.setPrice(takeOverPrice);
            detail.setQuantity(position.getPositionAmt().abs());
            detail.setTradeType(TradeType.LIQUIDATION);
            if (position.getPositionAmt().compareTo(BigDecimal.ZERO) > 0) {
                detail.setBuyClientId(insuranceClientId);
                detail.setBuyMatchRole(MatchRole.TAKER);
                detail.setSellClientId(clientEntity.getClientId());
                detail.setSellMatchRole(MatchRole.MAKER);
            } else {
                detail.setBuyClientId(clientEntity.getClientId());
                detail.setBuyMatchRole(MatchRole.MAKER);
                detail.setSellClientId(insuranceClientId);
                detail.setSellMatchRole(MatchRole.TAKER);
            }
            detailList.add(detail);

            BaseResponse response = tradeCommand.otcTrade(otcRequest);
            if (response.isError()) {
                log.error("take over position is fail during liquidation: {}, {}", JSON.toJSONString(otcRequest),
                    response.getMsg());
                return false;
            } else {
                log.info("take over position is success during liquidation: {}", JSON.toJSONString(otcRequest));
            }
        }
        return true;
    }

    /**
     * turn off client trade authority
     */
    private void turnOffClientTradeAuthority(String clientId) {
        setClientTradeAuthority(clientId, Constants.NO_TRADE_AUTHORITY);
    }

    /**
     * turn on client trade authority
     */
    private void turnOnClientTradeAuthority(String clientId) {
        setClientTradeAuthority(clientId, Constants.DEFAULT_TRADE_AUTHORITY);
    }

    /**
     * set client trade authority
     */
    private void setClientTradeAuthority(String clientId, Integer tradeAuthority) {
        List<TradeAuthorityRequest> requestList = new ArrayList<>();
        requestList.add(new TradeAuthorityRequest(clientId, tradeAuthority));
        tradeCommand.setClientTradeAuthority(requestList);
    }

    /**
     * insurance account cancel order
     */
    private void insuranceCancelOrder(String insuranceClientId) {
        ClientEntity clientEntity = localCache.getClientEntity(insuranceClientId);
        Collection<Order> orders = clientEntity.getOrders().values();
        for (Order order : orders) {
            CancelOrderRequest request = new CancelOrderRequest();
            request.setClientId(clientEntity.getClientId());
            request.setSymbol(order.getSymbol());
            request.setOrderId(order.getOrderId());
            tradeCommand.cancelOrder(request);
        }
    }

    /**
     * insurance account close position
     */
    private void insuranceClosePosition(String insuranceClientId) {
        ClientEntity clientEntity = localCache.getClientEntity(insuranceClientId);
        for (Position position : clientEntity.getPositions().values()) {
            if (position.getPositionAmt().compareTo(BigDecimal.ZERO) != 0) {
                SymbolInfo symbolInfo = localCache.getSymbolInfo(position.getSymbol());
                PlaceOrderRequest request = new PlaceOrderRequest();
                if (position.getPositionAmt().compareTo(BigDecimal.ZERO) > 0) {
                    request.setSide(OrderSide.SELL);
                } else {
                    request.setSide(OrderSide.BUY);
                }
                BigDecimal closePrice = getClosePrice(symbolInfo, request.getSide(), position.getOpenPrice());
                if (closePrice != null) {
                    request.setPrice(closePrice);
                    request.setClientId(position.getClientId());
                    request.setSymbol(position.getSymbol());
                    request.setType(OrderType.LIMIT);
                    request.setQuantity(position.getPositionAmt().abs());
                    request.setReduceOnly(true);
                    BaseResponse response = tradeCommand.placeOrder(request);
                    if (response.isError()) {
                        log.error("insurance close position order request is fail: {}, {}", JSON.toJSONString(request),
                            response.getMsg());
                    } else {
                        log.info("insurance close position order request is success: {}", JSON.toJSONString(request));
                    }
                }
            }
        }
    }

    /**
     * get total maintenance margin and unrealized profit of the positions
     * 
     * T1: maintenance margin
     * 
     * T2: unrealized profit
     * 
     * T3: clear asset balance
     */
    private Tuple3<BigDecimal, BigDecimal, BigDecimal> getCrossFunds(Collection<Position> positions,
        ClientEntity clientEntity) {
        BigDecimal totalMaintenanceMargin = BigDecimal.ZERO;
        BigDecimal totalUnrealizedProfit = BigDecimal.ZERO;
        BigDecimal clearAssetBalance = BigDecimal.ZERO;
        for (Position position : positions) {
            if (position.getMarginType() == MarginType.CROSSED
                && position.getPositionAmt().compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal markPrice = localCache.getSymbolIndicator(position.getSymbol()).getMarkPrice();
                if (markPrice == null) {
                    log.error("the mark price of {} is null", position.getSymbol());
                    return null;
                } else {
                    SymbolInfo symbolInfo = localCache.getSymbolInfo(position.getSymbol());

                    BigDecimal maintenanceMargin = position.getPositionAmt().abs().multiply(markPrice)
                        .multiply(symbolInfo.getVolumeMultiple()).multiply(symbolInfo.getMaintenanceMarginRate());
                    totalMaintenanceMargin = totalMaintenanceMargin.add(maintenanceMargin);
                    position.setMaintenanceMargin(maintenanceMargin);

                    BigDecimal unrealizedProfit = (markPrice.subtract(position.getOpenPrice()))
                        .multiply(position.getPositionAmt()).multiply(symbolInfo.getVolumeMultiple());
                    totalUnrealizedProfit = totalUnrealizedProfit.add(unrealizedProfit);
                    position.setUnrealizedProfit(unrealizedProfit);

                    position.setMarkPrice(markPrice);
                }
            }
        }
        AssetBalance assetBalance = clientEntity.getBalance(localCache.getSystemParameter(SystemParameter.CLEAR_ASSET));
        if (assetBalance != null) {
            clearAssetBalance = assetBalance.getBalance();
        }
        /**
         * It is safe to use collateral cache here.
         */
        clearAssetBalance = clearAssetBalance.add(localCache.getClientCollateral(clientEntity.getClientId()));
        return Tuples.of(totalMaintenanceMargin, totalUnrealizedProfit, clearAssetBalance);
    }

    /**
     * get the close position price
     * 
     * BUY: MAX{openPrice, markPrice, indexPrice} * 1.03
     * 
     * SELL: MIN{openPrice, markPrice, indexPrice} * 0.97
     */
    BigDecimal getClosePrice(SymbolInfo symbolInfo, OrderSide side, BigDecimal openPrice) {
        BigDecimal markPrice = localCache.getSymbolIndicator(symbolInfo.getSymbol()).getMarkPrice();
        if (markPrice == null) {
            log.error("the mark price of {} is null", symbolInfo.getSymbol());
            return null;
        }
        BigDecimal indexPrice = localCache.getSymbolIndicator(symbolInfo.getSymbol()).getIndexPrice();
        if (indexPrice == null) {
            log.error("the index price of {} is null", symbolInfo.getSymbol());
            return null;
        }
        BigDecimal closePrice;
        BigDecimal closePriceDiscount =
            new BigDecimal(localCache.getSystemParameter(SystemParameter.INSURANCE_CLOSE_POSITION_DISCOUNT));
        if (side == OrderSide.BUY) {
            closePrice =
                BigDecimalUtil.max(openPrice, markPrice, indexPrice).multiply(BigDecimal.ONE.add(closePriceDiscount));
        } else {
            closePrice = BigDecimalUtil.min(openPrice, markPrice, indexPrice)
                .multiply(BigDecimal.ONE.subtract(closePriceDiscount));
        }
        return BigDecimalUtil.getVal(closePrice, symbolInfo.getPriceAssetScale());
    }

    /**
     * check deduct when liquidation isn't triggered.
     */
    private boolean checkDeductNoLiquidation(String clientId) {
        boolean deduct = false;
        ClientEntity clientEntity = localCache.getClientEntity(clientId);
        String clearAsset = localCache.getSystemParameter(SystemParameter.CLEAR_ASSET);
        AssetBalance clearAssetBalance = clientEntity.getBalance(clearAsset);
        if (clearAssetBalance != null && clearAssetBalance.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal debtThreshold = new BigDecimal(localCache.getSystemParameter(SystemParameter.DEBT_THRESHOLD));
            if (clearAssetBalance.getBalance().negate().compareTo(debtThreshold) >= 0) {
                deduct = true;
            }
        }
        return deduct;
    }

    /**
     * check deduct after liquidation has been executed.
     */
    private boolean checkDeductAfterLiquidation(String clientId) {
        boolean deduct = false;
        ClientEntity clientEntity = localCache.getClientEntity(clientId);
        String clearAsset = localCache.getSystemParameter(SystemParameter.CLEAR_ASSET);
        AssetBalance clearAssetBalance = clientEntity.getBalance(clearAsset);
        if (clearAssetBalance != null && clearAssetBalance.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            deduct = true;
        }
        return deduct;
    }

    @Data
    @AllArgsConstructor
    public static class LiquidationSession {
        private Collection<Position> positions;
        private BigDecimal totalMaintenanceMargin;
        private BigDecimal totalUnrealizedProfit;
        private BigDecimal balance;
        private BigDecimal feeRate;
        private Long lastTradeId;
    }

}
