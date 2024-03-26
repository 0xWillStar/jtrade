package com.crypto.jtrade.core.provider.service.rule.impl.perpetual;

import java.math.BigDecimal;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.crypto.jtrade.common.constants.*;
import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.*;
import com.crypto.jtrade.common.util.BigDecimalUtil;
import com.crypto.jtrade.core.api.model.AdjustPositionMarginRequest;
import com.crypto.jtrade.core.api.model.DepositRequest;
import com.crypto.jtrade.core.api.model.PlaceOrderRequest;
import com.crypto.jtrade.core.api.model.WithdrawRequest;
import com.crypto.jtrade.core.provider.model.convert.BeanMapping;
import com.crypto.jtrade.core.provider.model.landing.*;
import com.crypto.jtrade.core.provider.model.session.OrderSession;
import com.crypto.jtrade.core.provider.service.cache.ClientEntity;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.landing.MySqlLanding;
import com.crypto.jtrade.core.provider.service.landing.RedisLanding;
import com.crypto.jtrade.core.provider.service.match.MatchEngine;
import com.crypto.jtrade.core.provider.service.match.MatchEngineManager;
import com.crypto.jtrade.core.provider.service.publish.PrivatePublish;
import com.crypto.jtrade.core.provider.service.rule.impl.AbstractTradeRule;
import com.crypto.jtrade.core.provider.util.SequenceHelper;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

/**
 * asset check
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class AssetCheckTradeRule extends AbstractTradeRule {

    @Getter
    private int sequence = 5;

    @Getter
    private long usedProductType = Constants.USE_PERPETUAL;

    @Getter
    private long usedCommand = Constants.USE_PLACE_ORDER | Constants.USE_DEPOSIT | Constants.USE_WITHDRAW
        | Constants.USE_ADJUST_POSITION_MARGIN | Constants.USE_DEDUCT_COLLATERAL;

    @Autowired
    private BeanMapping beanMapping;

    @Autowired
    private PrivatePublish privatePublish;

    @Autowired
    private RedisLanding redisLanding;

    @Autowired
    private MySqlLanding mySqlLanding;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private MatchEngineManager matchEngineManager;

    /**
     * deposit
     */
    @Override
    public void deposit(Long requestId, DepositRequest request) {
        DataAction balanceAction;
        DataAction debtClientAction = DataAction.NONE;
        /**
         * update local cache first
         */
        ClientEntity clientEntity = localCache.getClientEntity(request.getClientId());
        AssetBalance balance = clientEntity.getBalance(request.getAsset());
        if (balance == null) {
            balance = AssetBalance.createAssetBalance(request.getClientId(), request.getAsset(), request.getAmount());
            clientEntity.addBalance(balance);
            balanceAction = DataAction.INSERT;
        } else {
            balance.setBalance(balance.getBalance().add(request.getAmount()));
            balance.setDeposit(balance.getDeposit().add(request.getAmount()));
            balanceAction = DataAction.UPDATE;
        }
        // deposit bill
        Bill bill = Bill.createBill(request.getClientId(), null, BillType.DEPOSIT, request.getAsset(),
            request.getAmount(), null);

        /**
         * debtClientAction: client which no positions and have debts
         */
        if (request.getAsset().equals(localCache.getSystemParameter(SystemParameter.CLEAR_ASSET))
            && balance.getPositionAmt().compareTo(BigDecimal.ZERO) == 0
            && balance.getBalance().compareTo(BigDecimal.ZERO) > 0
            && balance.getBalance().compareTo(request.getAmount()) < 0) {
            localCache.getDebtClientIds().remove(request.getClientId());
            debtClientAction = DataAction.DELETE;
        } else {
            /**
             * invalidate collateral cache
             */
            localCache.invalidateClientCollateral(request.getClientId());
        }

        AssetBalance cpAssetBalance = beanMapping.clone(balance);
        /**
         * write to redis
         */
        DepositLanding landing = DepositLanding.builder().requestId(requestId).balanceAction(balanceAction)
            .balance(cpAssetBalance).bill(bill).debtClientAction(debtClientAction).build();
        redisLanding.deposit(landing);
        /**
         * write to mysql
         */
        mySqlLanding.deposit(landing);
        /**
         * private publish
         */
        privatePublish.publishComplex(new ComplexEntity(null, Collections.singletonList(cpAssetBalance), null, null,
            Collections.singletonList(bill)));
    }

    /**
     * withdraw
     */
    @Override
    public void withdraw(Long requestId, WithdrawRequest request) {
        ClientEntity clientEntity = localCache.getClientEntity(request.getClientId());
        checkWithdraw(clientEntity, request);

        /**
         * update local cache
         */
        AssetBalance balance = clientEntity.getBalance(request.getAsset());
        balance.setBalance(balance.getBalance().subtract(request.getAmount()));
        balance.setWithdraw(balance.getWithdraw().add(request.getAmount()));
        // deposit bill
        Bill bill = Bill.createBill(request.getClientId(), null, BillType.WITHDRAW, request.getAsset(),
            request.getAmount().negate(), null);

        /**
         * invalidate collateral cache
         */
        if (!request.getAsset().equals(localCache.getSystemParameter(SystemParameter.CLEAR_ASSET))) {
            localCache.invalidateClientCollateral(request.getClientId());
        }

        AssetBalance cpAssetBalance = beanMapping.clone(balance);
        /**
         * write to redis
         */
        WithdrawLanding landing = WithdrawLanding.builder().requestId(requestId).balanceAction(DataAction.UPDATE)
            .balance(cpAssetBalance).bill(bill).build();
        redisLanding.withdraw(landing);
        /**
         * write to mysql
         */
        mySqlLanding.withdraw(landing);
        /**
         * private publish
         */
        privatePublish.publishComplex(new ComplexEntity(null, Collections.singletonList(cpAssetBalance), null, null,
            Collections.singletonList(bill)));
    }

    /**
     * place order
     */
    @Override
    public void placeOrder(PlaceOrderRequest request, OrderSession session) {
        if ((request.getType() != OrderType.LIMIT) && (request.getType() != OrderType.MARKET)) {
            return;
        }

        BigDecimal frozenPrice = getFrozenPrice(request, session);
        Order order = checkOrderFund(request, session, frozenPrice);
        saveOrder(session, order);
    }

    /**
     * adjust position margin
     */
    @Override
    public void adjustPositionMargin(Long requestId, AdjustPositionMarginRequest request) {
        SymbolInfo symbolInfo = localCache.getSymbolInfo(request.getSymbol());
        if (symbolInfo == null) {
            throw new TradeException(TradeError.SYMBOL_NOT_EXIST);
        }

        ClientEntity clientEntity = localCache.getClientEntity(request.getClientId());
        Position position = clientEntity.getPosition(request.getSymbol());
        if (position == null || position.getMarginType() == MarginType.CROSSED) {
            throw new TradeException(TradeError.FORBID_ADJUST_POSITION_MARGIN);
        }
        checkAdjustAmount(request, symbolInfo, clientEntity, position);

        /**
         * update local cache
         */
        position.setIsolatedBalance(position.getIsolatedBalance().add(request.getAmount()));
        AssetBalance balance = clientEntity.getBalance(symbolInfo.getClearAsset());
        balance.setBalance(balance.getBalance().subtract(request.getAmount()));
        balance.setIsolatedBalance(balance.getIsolatedBalance().add(request.getAmount()));

        AssetBalance cpAssetBalance = beanMapping.clone(balance);
        Position cpPosition = beanMapping.clone(position);
        /**
         * write to redis
         */
        AdjustPositionMarginLanding landing = AdjustPositionMarginLanding.builder().requestId(requestId)
            .position(cpPosition).balance(cpAssetBalance).build();
        redisLanding.adjustPositionMargin(landing);
        /**
         * write to mysql
         */
        mySqlLanding.adjustPositionMargin(landing);
        /**
         * private publish
         */
        privatePublish
            .publishComplex(new ComplexEntity(null, Collections.singletonList(cpAssetBalance), cpPosition, null, null));
    }

    /**
     * deduct collateral asset
     */
    @Override
    public void deductCollateral(Long requestId, String clientId) {
        ClientEntity clientEntity = localCache.getClientEntity(clientId);
        String clearAsset = localCache.getSystemParameter(SystemParameter.CLEAR_ASSET);
        if (clientEntity.getBalance(clearAsset).getBalance().compareTo(BigDecimal.ZERO) >= 0) {
            return;
        }

        DataAction debtClientAction = DataAction.NONE;
        /**
         * sell side
         */
        AssetBalance sellClearBalance = clientEntity.getBalance(clearAsset);
        List<AssetBalance> sellBalanceList = new ArrayList<>();
        sellBalanceList.add(sellClearBalance);
        List<Bill> sellBillList = new ArrayList<>();

        /**
         * buy side
         */
        String swapClientId = localCache.getSystemParameter(SystemParameter.SWAP_CLIENT_ID);
        ClientEntity buyClientEntity = localCache.getClientEntity(swapClientId);
        AssetBalance buyClearBalance = buyClientEntity.getBalance(clearAsset);
        Map<AssetBalance, DataAction> buyBalanceMap = new HashMap<>();
        if (buyClearBalance == null) {
            buyClearBalance = AssetBalance.createAssetBalance(swapClientId, clearAsset, BigDecimal.ZERO);
            buyBalanceMap.put(buyClearBalance, DataAction.INSERT);
            // insert into local cache
            buyClientEntity.addBalance(buyClearBalance);
        } else {
            buyBalanceMap.put(buyClearBalance, DataAction.UPDATE);
        }
        List<Bill> buyBillList = new ArrayList<>();

        /**
         * deduct in order
         */
        AssetInfo clearAssetInfo = localCache.getAssetInfo(clearAsset);
        BigDecimal remainDebt = clientEntity.getBalance(clearAsset).getBalance().abs();
        List<AssetInfo> assetList = new ArrayList<>(localCache.getAllAssets().values());
        assetList.sort(Comparator.comparing(AssetInfo::getDeductOrder));
        for (AssetInfo assetInfo : assetList) {
            if (assetInfo.getAsset().equals(clearAsset)) {
                continue;
            }
            AssetBalance sellAssetBalance = clientEntity.getBalance(assetInfo.getAsset());
            if (sellAssetBalance != null && sellAssetBalance.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal markPrice = getMarkPrice(assetInfo.getIndexPriceSymbol());
                BigDecimal maxDeduct = BigDecimalUtil.divide(remainDebt,
                    (BigDecimal.ONE.subtract(assetInfo.getDiscount())).multiply(markPrice), assetInfo.getScale());

                BigDecimal swapQty = BigDecimal.ZERO;
                BigDecimal swapAmount = BigDecimal.ZERO;
                if (maxDeduct.compareTo(BigDecimal.ZERO) == 0) {
                    /**
                     * because of the scale of asset, maxDeduct after divide may be ZERO.
                     */
                    swapAmount = remainDebt;
                } else {
                    swapQty = BigDecimalUtil.min(sellAssetBalance.getBalance(), maxDeduct);
                    swapAmount = BigDecimalUtil.getVal(swapQty.multiply(markPrice), clearAssetInfo.getScale());
                }

                /**
                 * Seller's deduction
                 */
                // clear asset
                sellClearBalance.setBalance(sellClearBalance.getBalance().add(swapAmount));
                sellClearBalance.setMoneyChange(sellClearBalance.getMoneyChange().add(swapAmount));
                sellClearBalance.setUpdateTime(System.currentTimeMillis());
                sellBillList
                    .add(Bill.createBill(clientId, null, BillType.DEDUCT_COLLATERAL, clearAsset, swapAmount, null));
                // deduct asset
                if (swapQty.compareTo(BigDecimal.ZERO) != 0) {
                    sellAssetBalance.setBalance(sellAssetBalance.getBalance().subtract(swapQty));
                    sellAssetBalance.setMoneyChange(sellAssetBalance.getMoneyChange().subtract(swapQty));
                    sellAssetBalance.setUpdateTime(System.currentTimeMillis());
                    sellBalanceList.add(sellAssetBalance);
                    sellBillList.add(Bill.createBill(clientId, null, BillType.DEDUCT_COLLATERAL, assetInfo.getAsset(),
                        swapQty.negate(), null));
                }

                /**
                 * Buyer's deduction
                 */
                // clear asset
                buyClearBalance.setBalance(buyClearBalance.getBalance().subtract(swapAmount));
                buyClearBalance.setMoneyChange(buyClearBalance.getMoneyChange().subtract(swapAmount));
                buyClearBalance.setUpdateTime(System.currentTimeMillis());
                buyBillList.add(Bill.createBill(swapClientId, null, BillType.DEDUCT_COLLATERAL, clearAsset,
                    swapAmount.negate(), null));
                // deduct asset
                if (swapQty.compareTo(BigDecimal.ZERO) != 0) {
                    AssetBalance buyAssetBalance = buyClientEntity.getBalance(assetInfo.getAsset());
                    if (buyAssetBalance == null) {
                        buyAssetBalance =
                            AssetBalance.createAssetBalance(swapClientId, assetInfo.getAsset(), BigDecimal.ZERO);
                        buyBalanceMap.put(buyAssetBalance, DataAction.INSERT);
                        // insert into local cache
                        buyClientEntity.addBalance(buyAssetBalance);
                    } else {
                        buyBalanceMap.put(buyAssetBalance, DataAction.UPDATE);
                    }
                    buyAssetBalance.setBalance(buyAssetBalance.getBalance().add(swapQty));
                    buyAssetBalance.setMoneyChange(buyAssetBalance.getMoneyChange().add(swapQty));
                    buyAssetBalance.setUpdateTime(System.currentTimeMillis());
                    buyBillList.add(Bill.createBill(swapClientId, null, BillType.DEDUCT_COLLATERAL,
                        assetInfo.getAsset(), swapQty, null));
                }

                remainDebt = remainDebt.subtract(swapAmount);
            }
            if (remainDebt.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
        }
        if (sellClearBalance.getBalance().compareTo(BigDecimal.ZERO) >= 0
            && sellClearBalance.getPositionAmt().compareTo(BigDecimal.ZERO) == 0) {
            localCache.getDebtClientIds().remove(clientId);
            debtClientAction = DataAction.DELETE;
        }

        /**
         * prepare landing data
         */
        List<AssetBalance> cpSellBalanceList = new ArrayList<>();
        for (AssetBalance balance : sellBalanceList) {
            cpSellBalanceList.add(beanMapping.clone(balance));
        }
        Map<AssetBalance, DataAction> cpBuyBalanceMap = new HashMap<>();
        for (Map.Entry<AssetBalance, DataAction> entry : buyBalanceMap.entrySet()) {
            cpBuyBalanceMap.put(beanMapping.clone(entry.getKey()), entry.getValue());
        }
        DeductCollateralLanding landing = DeductCollateralLanding.builder().requestId(requestId).sellClientId(clientId)
            .sellBalanceList(cpSellBalanceList).sellBillList(sellBillList).buyClientId(swapClientId)
            .buyBalanceMap(cpBuyBalanceMap).buyBillList(buyBillList).debtClientAction(debtClientAction).build();
        /**
         * write to redis
         */
        redisLanding.deductCollateral(landing);
        /**
         * write to mysql
         */
        mySqlLanding.deductCollateral(landing);
        /**
         * private stream
         */
        privatePublish.publishComplex(new ComplexEntity(null, cpSellBalanceList, null, null, sellBillList));
        privatePublish.publishComplex(
            new ComplexEntity(null, new ArrayList<>(cpBuyBalanceMap.keySet()), null, null, buyBillList));
    }

    /**
     * get frozen price
     */
    private BigDecimal getFrozenPrice(PlaceOrderRequest request, OrderSession session) {
        /**
         * calculate frozenPrice for frozen margin or frozen fee:
         * 
         * 1. If order type is MARKET, frozenPrice is the highest bid or ask price which can reached.
         * 
         * 2. If order type is LIMIT, when side is SELL and price is lower than bid1 price, frozenPrice is the bid1
         * price; others frozenPrice is the order price.
         */
        BigDecimal frozenPrice = request.getPrice();
        Depth lastDepth = session.getLastDepth();

        if (request.getType() == OrderType.MARKET) {
            BigDecimal totalQty = BigDecimal.ZERO;
            List<Depth.Item> orderBook = request.getSide() == OrderSide.BUY ? lastDepth.getAsks() : lastDepth.getBids();
            if (CollectionUtils.isEmpty(orderBook)) {
                throw new TradeException(TradeError.NO_COUNTERPARTY);
            }
            for (Depth.Item item : orderBook) {
                totalQty = totalQty.add(item.getQuantity());
                frozenPrice = item.getPrice();
                if (totalQty.compareTo(request.getQuantity()) >= 0) {
                    break;
                }
            }
        }

        if (request.getType() == OrderType.LIMIT && request.getSide() == OrderSide.SELL) {
            if (!CollectionUtils.isEmpty(lastDepth.getBids())) {
                BigDecimal bid1Price = lastDepth.getBids().get(0).getPrice();
                if (request.getPrice().compareTo(bid1Price) < 0) {
                    frozenPrice = bid1Price;
                }
            }
        }

        return frozenPrice;
    }

    /**
     * check order fund
     */
    private Order checkOrderFund(PlaceOrderRequest request, OrderSession session, BigDecimal frozenPrice) {
        Order order = beanMapping.convert(request);

        boolean calculated = false;
        BigDecimal balance = BigDecimal.ZERO;
        BigDecimal totalFrozenFee = BigDecimal.ZERO;
        BigDecimal totalProfitAndMargin = BigDecimal.ZERO;

        SymbolInfo currSymbol = session.getAllSymbols().get(request.getSymbol());
        MarginType marginType = getMarginType(session.getClientEntity(), request.getSymbol());
        if (marginType == MarginType.ISOLATED) {
            order.setMarginType(MarginType.ISOLATED);
            Position position = session.getPositions().get(request.getSymbol());
            if (position != null) {
                order.setFirstIsolatedOrder(false);
                balance = position.getIsolatedBalance();
                totalFrozenFee = position.getIsolatedFrozenFee();
                totalProfitAndMargin = getProfitAndMargin(order, position, session, frozenPrice);
                calculated = true;
            } else {
                order.setFirstIsolatedOrder(true);
                balance = getMaxClearAssetWithdrawAmount(currSymbol.getClearAsset(), session.getClientEntity());
            }

        } else {
            order.setMarginType(MarginType.CROSSED);
            order.setFirstIsolatedOrder(false);

            AssetBalance assetBalance = session.getAssetBalance();
            /**
             * balance = balance of the clear asset + balance of all collateral assets
             */
            balance = assetBalance.getBalance().add(localCache.getClientCollateral(request.getClientId()));
            totalFrozenFee = assetBalance.getFrozenFee();

            /**
             * calculate unrealizedProfit and margin: ∑(unrealizedProfit - MAX{longUseMargin + longFrozenMargin,
             * shortUseMargin + shortFrozenMargin})
             */
            for (Position position : session.getPositions().values()) {
                if (position.getMarginType() == MarginType.CROSSED
                    && (position.getLongFrozenMargin().compareTo(BigDecimal.ZERO) > 0
                        || position.getShortFrozenMargin().compareTo(BigDecimal.ZERO) > 0
                        || position.getPositionAmt().compareTo(BigDecimal.ZERO) != 0)) {
                    totalProfitAndMargin =
                        totalProfitAndMargin.add(getProfitAndMargin(order, position, session, frozenPrice));
                    if (position.getSymbol().equals(order.getSymbol())) {
                        calculated = true;
                    }
                }
            }
        }

        /**
         * If the positions does not include the symbol of the order, the margin is calculated separately.
         */
        if (!calculated) {
            BigDecimal leverage = getLeverage(session, request.getSymbol());
            BigDecimal frozenMargin =
                getMargin(frozenPrice, request.getQuantity(), leverage, currSymbol.getVolumeMultiple());
            totalProfitAndMargin = totalProfitAndMargin.subtract(frozenMargin);

            order.setLeverage(leverage);
            order.setFrozenMargin(frozenMargin);
        }

        /**
         * calculate frozen fee
         */
        BigDecimal frozenFee = getFrozenFee(frozenPrice, request.getQuantity(), session.getFeeRate().getTaker(),
            currSymbol.getVolumeMultiple());
        order.setFrozenFee(frozenFee);

        /**
         * ReduceOnly order is not need to check available funds
         */
        if (!order.getReduceOnly()) {
            /**
             * check available funds
             * 
             * available = balance + ∑(unrealizedProfit - MAX{longUseMargin + longFrozenMargin, shortUseMargin +
             * shortFrozenMargin}) - ∑(frozenFee)
             */
            BigDecimal available = balance.add(totalProfitAndMargin).subtract(totalFrozenFee.add(frozenFee));
            if (available.compareTo(BigDecimal.ZERO) < 0) {
                throw new TradeException(TradeError.INSUFFICIENT_FUNDS);
            }
        }

        return order;
    }

    /**
     * save the result, update local cache and write to redis
     */
    private void saveOrder(OrderSession session, Order order) {
        SymbolInfo currSymbol = session.getAllSymbols().get(order.getSymbol());
        order.setFrozenFee(BigDecimalUtil.getVal(order.getFrozenFee(), currSymbol.getClearAssetScale()));
        order.setFrozenMargin(BigDecimalUtil.getVal(order.getFrozenMargin(), currSymbol.getClearAssetScale()));
        order.setOrderTime(System.currentTimeMillis());
        order.setUpdateTime(order.getOrderTime());
        order.setStatus(OrderStatus.NEW);
        order.setLeftQty(order.getQuantity());
        if (session.isStopTriggered()) {
            Order oldOrder = session.getOrder();
            order.setOrderId(oldOrder.getOrderId());
            order.setOrigType(oldOrder.getOrigType());
        } else {
            order.setOrderId(SequenceHelper.incrementAndGetOrderId());
        }
        if (order.getOtoOrderType() != OTOOrderType.NONE) {
            order.setSubOrderId1(session.getSubOrderId1());
            order.setSubOrderId2(session.getSubOrderId2());
        }

        /**
         * update local cache
         */
        // update order
        session.setOrder(order);
        session.getOrders().put(order.getOrderId(), order);
        // update position
        Position position = session.getPositions().get(order.getSymbol());
        DataAction positionAction;
        if (position == null) {
            position = Position.createPosition(order, order.getMarginType(), currSymbol.getClearAsset());
            session.getPositions().put(order.getSymbol(), position);
            positionAction = DataAction.INSERT;
        } else {
            position.setLongFrozenAmt(position.getLongFrozenAmt()
                .add(order.getSide() == OrderSide.BUY ? order.getQuantity() : BigDecimal.ZERO));
            position.setShortFrozenAmt(position.getShortFrozenAmt()
                .add(order.getSide() == OrderSide.SELL ? order.getQuantity() : BigDecimal.ZERO));
            position.setLongFrozenMargin(position.getLongFrozenMargin()
                .add(order.getSide() == OrderSide.BUY ? order.getFrozenMargin() : BigDecimal.ZERO));
            position.setShortFrozenMargin(position.getShortFrozenMargin()
                .add(order.getSide() == OrderSide.SELL ? order.getFrozenMargin() : BigDecimal.ZERO));
            position.setUpdateTime(System.currentTimeMillis());
            positionAction = DataAction.UPDATE;
        }

        /**
         * If ReduceOnly orders may result in a reverse opening, cancel ReduceOnly orders.
         */
        if (position.getReduceOnlyOrderCount() > 0) {
            cancelReduceOnlyOrder(session.getOrders().values(), order, position);
        }
        /**
         * If the order is ReduceOnly, increase the ReduceOnlyOrderCount
         */
        if (order.getReduceOnly()) {
            position.setReduceOnlyOrderCount(position.getReduceOnlyOrderCount() + 1);
        }
        // update balance
        AssetBalance assetBalance = session.getAssetBalance();
        if (order.getMarginType() == MarginType.CROSSED) {
            assetBalance.setFrozenFee(assetBalance.getFrozenFee().add(order.getFrozenFee()));
            assetBalance.setUpdateTime(System.currentTimeMillis());
        } else {
            /**
             * Isolated Margin Mode
             */
            if (order.getFirstIsolatedOrder()) {
                /**
                 * transfer frozenMargin+frozenFee from AssetBalance to position
                 */
                assetBalance.setIsolatedBalance(
                    assetBalance.getIsolatedBalance().add(order.getFrozenMargin()).add(order.getFrozenFee()));
                assetBalance.setBalance(
                    assetBalance.getBalance().subtract(order.getFrozenMargin()).subtract(order.getFrozenFee()));
                assetBalance.setUpdateTime(System.currentTimeMillis());
                position.setIsolatedBalance(
                    position.getIsolatedBalance().add(order.getFrozenMargin()).add(order.getFrozenFee()));
            }
            position.setIsolatedFrozenFee(position.getIsolatedFrozenFee().add(order.getFrozenFee()));
        }

        /**
         * add the clientId to orderClientIds.
         */
        DataAction orderClientAction = DataAction.NONE;
        if (!localCache.getOrderClientIds().contains(order.getClientId())) {
            localCache.getOrderClientIds().add(order.getClientId());
            orderClientAction = DataAction.INSERT;
        }

        Order cpOrder = beanMapping.clone(order);
        Position cpPosition = beanMapping.clone(position);
        AssetBalance cpAssetBalance = beanMapping.clone(assetBalance);
        /**
         * write to redis. Using constructor is more efficient than using builder.
         */
        PlaceOrderLanding landing =
            new PlaceOrderLanding(session.getRequestId(), cpOrder, DataAction.INSERT, cpPosition, positionAction,
                cpAssetBalance, session.getBalanceAction(), orderClientAction, session.isStopTriggered());
        redisLanding.placeOrder(landing);
        /**
         * write to mysql
         */
        mySqlLanding.placeOrder(landing);
        /**
         * private publish
         */
        privatePublish.publishComplex(
            new ComplexEntity(cpOrder, Collections.singletonList(cpAssetBalance), cpPosition, null, null));
    }

    /**
     * calculate profit and margin
     */
    private BigDecimal getProfitAndMargin(Order order, Position position, OrderSession session,
        BigDecimal frozenPrice) {
        SymbolInfo symbolInfo = session.getAllSymbols().get(position.getSymbol());
        BigDecimal leverage = getLeverage(session, position.getSymbol());

        BigDecimal unrealizedProfit = BigDecimal.ZERO;
        BigDecimal longUseMargin = BigDecimal.ZERO;
        BigDecimal shortUseMargin = BigDecimal.ZERO;
        if (position.getPositionAmt().compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal markPrice = getMarkPrice(session, position.getSymbol());
            unrealizedProfit = getUnrealizedProfit(position, markPrice, symbolInfo.getVolumeMultiple());
            BigDecimal useMargin =
                getMargin(markPrice, position.getPositionAmt(), leverage, symbolInfo.getVolumeMultiple());
            if (useMargin.compareTo(BigDecimal.ZERO) > 0) {
                longUseMargin = useMargin;
            } else {
                shortUseMargin = useMargin.abs();
            }
        }

        BigDecimal longFrozenMargin = position.getLongFrozenMargin();
        BigDecimal shortFrozenMargin = position.getShortFrozenMargin();
        if (position.getSymbol().equals(order.getSymbol())) {
            BigDecimal frozenMargin =
                getMargin(frozenPrice, order.getQuantity(), leverage, symbolInfo.getVolumeMultiple());
            if (order.getSide() == OrderSide.BUY) {
                longFrozenMargin = longFrozenMargin.add(frozenMargin);
            } else {
                shortFrozenMargin = shortFrozenMargin.add(frozenMargin);
            }

            order.setLeverage(leverage);
            order.setFrozenMargin(frozenMargin);
        }

        BigDecimal longMargin = longUseMargin.add(longFrozenMargin);
        BigDecimal shortMargin = shortUseMargin.add(shortFrozenMargin);

        return unrealizedProfit.subtract(longMargin.compareTo(shortMargin) > 0 ? longMargin : shortMargin);
    }

    /**
     * If ReduceOnly orders may result in a reverse opening, cancel ReduceOnly orders.
     * 
     * @param orderCollection orderCollection has includes the new order
     * @param newOrder the new order
     * @param position the position
     */
    private void cancelReduceOnlyOrder(Collection<Order> orderCollection, Order newOrder, Position position) {
        BigDecimal positionAmt = position.getPositionAmt();
        if ((positionAmt.compareTo(BigDecimal.ZERO) > 0 && newOrder.getSide() == OrderSide.BUY)
            || (positionAmt.compareTo(BigDecimal.ZERO) < 0 && newOrder.getSide() == OrderSide.SELL)) {
            return;
        }

        List<Order> orderList = new ArrayList<>();
        BigDecimal totalQty = BigDecimal.ZERO;
        for (Order order : orderCollection) {
            if (order.getSymbol().equals(newOrder.getSymbol()) && order.getSide() == newOrder.getSide()) {
                if (order.getType() == OrderType.MARKET) {
                    totalQty = totalQty.add(order.getLeftQty());
                } else {
                    orderList.add(order);
                }
            }
        }

        if (newOrder.getSide() == OrderSide.SELL) {
            orderList.sort((o1, o2) -> {
                if (o1.getPrice().compareTo(o2.getPrice()) == 0) {
                    return o1.getOrderId().compareTo(o2.getOrderId());
                } else {
                    return o1.getPrice().compareTo(o2.getPrice());
                }
            });
        } else if (newOrder.getSide() == OrderSide.BUY) {
            orderList.sort((o1, o2) -> {
                if (o1.getPrice().compareTo(o2.getPrice()) == 0) {
                    return o1.getOrderId().compareTo(o2.getOrderId());
                } else {
                    return o2.getPrice().compareTo(o1.getPrice());
                }
            });
        }

        positionAmt = positionAmt.abs();
        boolean canCancel = false;
        MatchEngine matchEngine = matchEngineManager.getMatchEngine(newOrder.getSymbol());
        for (Order order : orderList) {
            if (!canCancel) {
                totalQty = totalQty.add(order.getLeftQty());
                if (totalQty.compareTo(positionAmt) > 0) {
                    canCancel = true;
                }
            }
            if (canCancel && order.getReduceOnly()) {
                matchEngine.cancelOrder(order);
            }
        }
    }

    /**
     * check withdraw fund
     */
    private void checkWithdraw(ClientEntity clientEntity, WithdrawRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new TradeException(TradeError.REQUEST_ILLEGAL);
        }
        if (clientEntity.getBalance(request.getAsset()) == null) {
            throw new TradeException(TradeError.INSUFFICIENT_FUNDS_AVAILABLE);
        }

        String clearAsset = localCache.getSystemParameter(SystemParameter.CLEAR_ASSET);
        Tuple4<BigDecimal, BigDecimal, BigDecimal, BigDecimal> funds = getCrossFunds(clearAsset, clientEntity);
        BigDecimal crossAvailable = funds.getT1();
        BigDecimal clearAssetBalance = funds.getT3();
        BigDecimal crossUnrealizedProfit = funds.getT4();
        BigDecimal maxWithdrawAmount = BigDecimal.ZERO;
        if (request.getAsset().equals(clearAsset)) {
            /**
             * max withdraw = MIN[clear asset balance, available - MAX(0, unrealized profit)] - withdraw fee
             */
            maxWithdrawAmount = BigDecimalUtil.min(clearAssetBalance,
                crossAvailable.subtract(BigDecimalUtil.max(BigDecimal.ZERO, crossUnrealizedProfit)));
        } else {
            /**
             * max withdraw = MIN[balance, (available - MAX(0, unrealized profit) - withdraw fee)/(index price * (1 -
             * discount))]
             */
            AssetInfo assetInfo = localCache.getAssetInfo(request.getAsset());
            if (assetInfo == null) {
                throw new TradeException(TradeError.ASSET_NOT_EXIST);
            }
            BigDecimal indexPrice = localCache.getSymbolIndicator(assetInfo.getIndexPriceSymbol()).getIndexPrice();
            if (indexPrice == null) {
                throw new TradeException(TradeError.INDEX_PRICE_NOT_EXIST);
            }
            BigDecimal balanceLimit = BigDecimalUtil.divide(
                crossAvailable.subtract(BigDecimalUtil.max(BigDecimal.ZERO, crossUnrealizedProfit)),
                indexPrice.multiply(BigDecimal.ONE.subtract(assetInfo.getDiscount())));
            maxWithdrawAmount =
                BigDecimalUtil.min(clientEntity.getBalance(request.getAsset()).getBalance(), balanceLimit);
        }
        if (request.getAmount().compareTo(maxWithdrawAmount) > 0) {
            throw new TradeException(TradeError.INSUFFICIENT_FUNDS_AVAILABLE);
        }
    }

    /**
     * check adjust position margin
     */
    private void checkAdjustAmount(AdjustPositionMarginRequest request, SymbolInfo symbolInfo,
        ClientEntity clientEntity, Position position) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            /**
             * Isolated Margin Mode: only the clear asset can be used as margin.
             * 
             * max clear asset withdraw amount: MIN[clear asset balance, available - MAX(0, unrealized profit)]
             */
            BigDecimal maxClearAssetWithdrawAmount =
                getMaxClearAssetWithdrawAmount(symbolInfo.getClearAsset(), clientEntity);
            if (request.getAmount().compareTo(maxClearAssetWithdrawAmount) > 0) {
                throw new TradeException(TradeError.INSUFFICIENT_FUNDS_AVAILABLE);
            }

        } else if (request.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            /**
             * max isolated withdraw amount: MIN(isolatedBalance - isolatedMaintenanceMargin, isolatedAvailable)
             */
            BigDecimal maxIsolatedWithdrawAmount = getMaxIsolatedWithdrawAmount(symbolInfo, position, clientEntity);
            if (request.getAmount().abs().compareTo(maxIsolatedWithdrawAmount) > 0) {
                throw new TradeException(TradeError.INSUFFICIENT_FUNDS_AVAILABLE);
            }

        } else {
            throw new TradeException(TradeError.ARGUMENT_INVALID);
        }
    }

    /**
     * get max clear asset withdraw amount: MIN[clear asset balance, available - MAX(0, unrealized profit)]
     */
    private BigDecimal getMaxClearAssetWithdrawAmount(String clearAsset, ClientEntity clientEntity) {
        Tuple4<BigDecimal, BigDecimal, BigDecimal, BigDecimal> funds = getCrossFunds(clearAsset, clientEntity);
        BigDecimal crossAvailable = funds.getT1();
        BigDecimal clearAssetBalance = funds.getT3();
        BigDecimal crossUnrealizedProfit = funds.getT4();
        return BigDecimalUtil.min(clearAssetBalance,
            crossAvailable.subtract(BigDecimalUtil.max(BigDecimal.ZERO, crossUnrealizedProfit)));
    }

    /**
     * get max isolated withdraw amount: MIN(isolatedBalance - isolatedMaintenanceMargin, isolatedAvailable)
     */
    private BigDecimal getMaxIsolatedWithdrawAmount(SymbolInfo symbolInfo, Position position,
        ClientEntity clientEntity) {
        BigDecimal markPrice = getMarkPrice(position.getSymbol());
        BigDecimal leverage = getLeverage(clientEntity, position.getSymbol());
        BigDecimal isolatedBalance = position.getIsolatedBalance();
        BigDecimal isolatedMaintenanceMargin = getMaintenanceMargin(markPrice, position.getPositionAmt().abs(),
            symbolInfo.getMaintenanceMarginRate(), symbolInfo.getVolumeMultiple());

        BigDecimal unrealizedProfit = BigDecimal.ZERO;
        BigDecimal longUseMargin = BigDecimal.ZERO;
        BigDecimal shortUseMargin = BigDecimal.ZERO;
        if (position.getPositionAmt().compareTo(BigDecimal.ZERO) != 0) {
            unrealizedProfit = getUnrealizedProfit(position, markPrice, symbolInfo.getVolumeMultiple());
            BigDecimal useMargin =
                getMargin(markPrice, position.getPositionAmt(), leverage, symbolInfo.getVolumeMultiple());
            if (useMargin.compareTo(BigDecimal.ZERO) > 0) {
                longUseMargin = useMargin;
            } else {
                shortUseMargin = useMargin.abs();
            }
        }

        BigDecimal longFrozenMargin = position.getLongFrozenMargin();
        BigDecimal shortFrozenMargin = position.getShortFrozenMargin();

        BigDecimal longMargin = longUseMargin.add(longFrozenMargin);
        BigDecimal shortMargin = shortUseMargin.add(shortFrozenMargin);

        BigDecimal profitAndMargin =
            unrealizedProfit.subtract(longMargin.compareTo(shortMargin) > 0 ? longMargin : shortMargin);
        BigDecimal isolatedAvailable = isolatedBalance.add(profitAndMargin).subtract(position.getIsolatedFrozenFee());

        return BigDecimalUtil.min(isolatedBalance.subtract(isolatedMaintenanceMargin), isolatedAvailable);
    }

    /**
     * get cross available、 total cross maintenance margin、clear asset balance
     * 
     * T1: cross available
     * 
     * T2: total cross maintenance margin
     * 
     * T3: clear asset balance
     * 
     * T4: cross unrealized profit
     */
    private Tuple4<BigDecimal, BigDecimal, BigDecimal, BigDecimal> getCrossFunds(String clearAsset,
        ClientEntity clientEntity) {
        BigDecimal totalCrossProfitAndMargin = BigDecimal.ZERO;
        BigDecimal totalCrossMaintenanceMargin = BigDecimal.ZERO;
        BigDecimal totalCrossUnrealizedProfit = BigDecimal.ZERO;
        for (Position position : clientEntity.getPositions().values()) {
            if (position.getMarginType() == MarginType.CROSSED
                && (position.getLongFrozenMargin().compareTo(BigDecimal.ZERO) > 0
                    || position.getShortFrozenMargin().compareTo(BigDecimal.ZERO) > 0
                    || position.getPositionAmt().compareTo(BigDecimal.ZERO) != 0)) {
                SymbolInfo symbolInfo = localCache.getAllSymbols().get(position.getSymbol());
                BigDecimal leverage = getLeverage(clientEntity, position.getSymbol());

                BigDecimal unrealizedProfit = BigDecimal.ZERO;
                BigDecimal longUseMargin = BigDecimal.ZERO;
                BigDecimal shortUseMargin = BigDecimal.ZERO;
                if (position.getPositionAmt().compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal markPrice = getMarkPrice(position.getSymbol());
                    unrealizedProfit = getUnrealizedProfit(position, markPrice, symbolInfo.getVolumeMultiple());
                    BigDecimal useMargin =
                        getMargin(markPrice, position.getPositionAmt(), leverage, symbolInfo.getVolumeMultiple());
                    if (useMargin.compareTo(BigDecimal.ZERO) > 0) {
                        longUseMargin = useMargin;
                    } else {
                        shortUseMargin = useMargin.abs();
                    }
                    totalCrossMaintenanceMargin =
                        totalCrossMaintenanceMargin.add(getMaintenanceMargin(markPrice, position.getPositionAmt().abs(),
                            symbolInfo.getMaintenanceMarginRate(), symbolInfo.getVolumeMultiple()));
                }

                BigDecimal longFrozenMargin = position.getLongFrozenMargin();
                BigDecimal shortFrozenMargin = position.getShortFrozenMargin();

                BigDecimal longMargin = longUseMargin.add(longFrozenMargin);
                BigDecimal shortMargin = shortUseMargin.add(shortFrozenMargin);

                totalCrossProfitAndMargin = totalCrossProfitAndMargin
                    .add(unrealizedProfit.subtract(longMargin.compareTo(shortMargin) > 0 ? longMargin : shortMargin));
                totalCrossUnrealizedProfit = totalCrossUnrealizedProfit.add(unrealizedProfit);
            }
        }

        AssetBalance assetBalance = clientEntity.getBalance(clearAsset);
        if (assetBalance == null) {
            assetBalance = AssetBalance.createAssetBalance(clientEntity.getClientId(), clearAsset, BigDecimal.ZERO);
        }
        /**
         * total balance = balance of the clear asset + balance of all collateral assets
         */
        BigDecimal totalBalance =
            assetBalance.getBalance().add(localCache.getClientCollateral(clientEntity.getClientId()));
        BigDecimal crossAvailable = totalBalance.add(totalCrossProfitAndMargin).subtract(assetBalance.getFrozenFee());
        return Tuples.of(crossAvailable, totalCrossMaintenanceMargin, assetBalance.getBalance(),
            totalCrossUnrealizedProfit);
    }

    /**
     * get leverage
     */
    BigDecimal getLeverage(OrderSession session, String symbol) {
        BigDecimal leverage = null;
        ClientSetting setting = session.getSettings().get(symbol);
        if (setting != null) {
            leverage = setting.getLeverage();
        } else {
            setting = session.getSettings().get(Constants.DEFAULT);
            if (setting != null) {
                leverage = setting.getLeverage();
            }
        }
        return leverage == null ? session.getAllSymbols().get(symbol).getDefaultLeverage() : leverage;
    }

    /**
     * get leverage
     */
    BigDecimal getLeverage(ClientEntity clientEntity, String symbol) {
        BigDecimal leverage = null;
        ClientSetting setting = clientEntity.getSetting(symbol);
        if (setting != null) {
            leverage = setting.getLeverage();
        } else {
            setting = clientEntity.getSetting(Constants.DEFAULT);
            if (setting != null) {
                leverage = setting.getLeverage();
            }
        }
        return leverage == null ? localCache.getAllSymbols().get(symbol).getDefaultLeverage() : leverage;
    }

    /**
     * get marginType
     */
    MarginType getMarginType(ClientEntity clientEntity, String symbol) {
        MarginType marginType = null;
        ClientSetting setting = clientEntity.getSetting(symbol);
        if (setting != null) {
            marginType = setting.getMarginType();
        } else {
            setting = clientEntity.getSetting(Constants.DEFAULT);
            if (setting != null) {
                marginType = setting.getMarginType();
            }
        }
        return marginType == null ? MarginType.CROSSED : marginType;
    }

    /**
     * get mark price
     */
    BigDecimal getMarkPrice(OrderSession session, String symbol) {
        BigDecimal markPrice = null;
        SymbolIndicator indicator = session.getSymbolIndicators().get(symbol);
        if (indicator != null) {
            markPrice = indicator.getMarkPrice();
        }
        if (markPrice == null) {
            throw new TradeException(TradeError.MARK_PRICE_NOT_EXIST);
        }
        return markPrice;
    }

    /**
     * get mark price
     */
    BigDecimal getMarkPrice(String symbol) {
        BigDecimal markPrice = null;
        SymbolIndicator indicator = localCache.getSymbolIndicators().get(symbol);
        if (indicator != null) {
            markPrice = indicator.getMarkPrice();
        }
        if (markPrice == null) {
            throw new TradeException(TradeError.MARK_PRICE_NOT_EXIST);
        }
        return markPrice;
    }

    /**
     * calculate unrealized profit. If PositionType is NET, unrealized profit = (markPrice - openPrice) * positionAmount
     * * volumeMultiple.
     */
    BigDecimal getUnrealizedProfit(Position position, BigDecimal markPrice, BigDecimal volumeMultiple) {
        return (markPrice.subtract(position.getOpenPrice())).multiply(position.getPositionAmt())
            .multiply(volumeMultiple);
    }

    /**
     * calculate margin = price * quantity * volumeMultiple / leverage
     */
    BigDecimal getMargin(BigDecimal price, BigDecimal quantity, BigDecimal leverage, BigDecimal volumeMultiple) {
        return leverage.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
            : BigDecimalUtil.divide(price.multiply(quantity).multiply(volumeMultiple), leverage);
    }

    /**
     * calculate maintenance margin = price * quantity * volumeMultiple * maintenanceMarginRate
     */
    BigDecimal getMaintenanceMargin(BigDecimal price, BigDecimal quantity, BigDecimal maintenanceMarginRate,
        BigDecimal volumeMultiple) {
        return price.multiply(quantity).multiply(volumeMultiple).multiply(maintenanceMarginRate);
    }

    /**
     * calculate frozen fee = frozenPrice * quantity * volumeMultiple * feeRate
     */
    BigDecimal getFrozenFee(BigDecimal frozenPrice, BigDecimal quantity, BigDecimal feeRate,
        BigDecimal volumeMultiple) {
        return frozenPrice.multiply(quantity).multiply(feeRate).multiply(volumeMultiple);
    }

}
