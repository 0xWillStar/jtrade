package com.crypto.jtrade.core.provider.service.rule.impl.perpetual;

import java.math.BigDecimal;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.*;
import com.crypto.jtrade.common.model.*;
import com.crypto.jtrade.common.util.BigDecimalUtil;
import com.crypto.jtrade.core.provider.model.convert.BeanMapping;
import com.crypto.jtrade.core.provider.model.landing.OrderCanceledLanding;
import com.crypto.jtrade.core.provider.model.landing.OrderMatchedLanding;
import com.crypto.jtrade.core.provider.model.session.TradeSession;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.landing.MySqlLanding;
import com.crypto.jtrade.core.provider.service.landing.RedisLanding;
import com.crypto.jtrade.core.provider.service.publish.PrivatePublish;
import com.crypto.jtrade.core.provider.service.rule.impl.AbstractTradeRule;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * match handler
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class MatchTradeRule extends AbstractTradeRule {

    @Getter
    private int sequence = 1;

    @Getter
    private long usedProductType = Constants.USE_PERPETUAL;

    @Getter
    private long usedCommand = Constants.USE_ORDER_MATCHED | Constants.USE_ORDER_CANCELED;

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

    /**
     * order matched
     */
    @Override
    public void orderMatched(TradeSession session, BigDecimal fillPrice, BigDecimal fillQty, MatchRole matchRole,
        Long tradeId) {
        createTrade(session, fillPrice, fillQty, matchRole, tradeId);
        updatePositionMatched(session, fillPrice, fillQty);
        if (session.getTradeType() == TradeType.STANDARD) {
            updateOrderMatched(session, fillPrice, fillQty);
        }
        updateBalanceMatched(session);
        saveMatched(session);
    }

    /**
     * order canceled
     */
    @Override
    public void orderCanceled(TradeSession session) {
        updatePositionCanceled(session);
        updateOrderCanceled(session);
        updateBalanceCanceled(session);
        saveCanceled(session);
    }

    /**
     * create trade when matched
     */
    private void createTrade(TradeSession session, BigDecimal fillPrice, BigDecimal fillQty, MatchRole matchRole,
        Long tradeId) {
        Trade trade = Trade.builder().exchangeId(session.getOrder().getExchangeId())
            .memberId(session.getOrder().getMemberId()).clientId(session.getOrder().getClientId())
            .symbol(session.getOrder().getSymbol()).tradeId(tradeId).side(session.getOrder().getSide())
            .positionSide(session.getOrder().getPositionSide()).orderId(session.getOrder().getOrderId())
            .clientOrderId(session.getOrder().getClientOrderId()).price(fillPrice).qty(fillQty)
            .quoteQty(BigDecimalUtil.getVal(fillPrice.multiply(fillQty), session.getSymbolInfo().getPriceAssetScale()))
            .feeAsset(session.getSymbolInfo().getClearAsset()).matchRole(matchRole)
            .tradeTime(System.currentTimeMillis()).tradeType(session.getTradeType()).closeProfit(BigDecimal.ZERO)
            .build();
        BigDecimal feeRate =
            matchRole == MatchRole.MAKER ? session.getFeeRate().getMaker() : session.getFeeRate().getTaker();
        trade.setFee(
            BigDecimalUtil.getVal(trade.getQuoteQty().multiply(feeRate), session.getSymbolInfo().getClearAssetScale()));
        // save to session
        session.setTrade(trade);
    }

    /**
     * update position when matched
     */
    private void updatePositionMatched(TradeSession session, BigDecimal fillPrice, BigDecimal fillQty) {
        Order order = session.getOrder();
        Position position = session.getPosition();
        SymbolInfo symbolInfo = session.getSymbolInfo();

        BigDecimal oldPositionAmt = position.getPositionAmt().abs();
        BigDecimal closeProfit = BigDecimal.ZERO;
        BigDecimal closeAmt = BigDecimal.ZERO;
        /**
         * close position
         */
        if ((position.getPositionAmt().compareTo(BigDecimal.ZERO) > 0 && order.getSide() == OrderSide.SELL)
            || (position.getPositionAmt().compareTo(BigDecimal.ZERO) < 0 && order.getSide() == OrderSide.BUY)) {
            closeAmt =
                position.getPositionAmt().abs().compareTo(fillQty) > 0 ? fillQty : position.getPositionAmt().abs();
            closeAmt = order.getSide() == OrderSide.SELL ? closeAmt : closeAmt.negate();

            BigDecimal openPrice = position.getOpenPrice();
            closeProfit = (fillPrice.subtract(openPrice)).multiply(closeAmt).multiply(symbolInfo.getVolumeMultiple());
            closeProfit = BigDecimalUtil.getVal(closeProfit, symbolInfo.getClearAssetScale());
            // set closeProfit to trade
            session.getTrade().setCloseProfit(closeProfit);

            // set positionAmt
            position.setPositionAmt(position.getPositionAmt().subtract(closeAmt));
        }

        /**
         * open position
         */
        if (fillQty.compareTo(closeAmt.abs()) > 0) {
            BigDecimal openAmt = fillQty.subtract(closeAmt.abs());
            // position cost
            BigDecimal positionCost =
                openAmt.multiply(fillPrice).add(position.getPositionAmt().abs().multiply(position.getOpenPrice()));

            // set positionAmt
            position.setPositionAmt(
                position.getPositionAmt().add(order.getSide() == OrderSide.SELL ? openAmt.negate() : openAmt));
            // set open price
            BigDecimal openPrice = BigDecimalUtil.divide(positionCost, position.getPositionAmt().abs());
            position.setOpenPrice(BigDecimalUtil.getVal(openPrice, symbolInfo.getPriceAssetScale()));
        }

        if (session.getTradeType() == TradeType.STANDARD) {
            /**
             * Unfreeze margin and fee proportionally
             */
            BigDecimal unfreezeRate =
                BigDecimalUtil.divide(fillQty, order.getQuantity().subtract(order.getExecutedQty()));
            BigDecimal unfreezeMargin = order.getFrozenMargin().multiply(unfreezeRate);
            unfreezeMargin = BigDecimalUtil.getVal(unfreezeMargin, symbolInfo.getClearAssetScale());
            BigDecimal unfreezeFee = order.getFrozenFee().multiply(unfreezeRate);
            unfreezeFee = BigDecimalUtil.getVal(unfreezeFee, symbolInfo.getClearAssetScale());
            // set to session
            session.setUnfreezeMargin(unfreezeMargin);
            session.setUnfreezeFee(unfreezeFee);

            /**
             * update frozenAmt and frozenMargin of the position
             */
            if (order.getSide() == OrderSide.SELL) {
                position.setShortFrozenAmt(position.getShortFrozenAmt().subtract(fillQty));
                position.setShortFrozenMargin(position.getShortFrozenMargin().subtract(unfreezeMargin));
            } else {
                position.setLongFrozenAmt(position.getLongFrozenAmt().subtract(fillQty));
                position.setLongFrozenMargin(position.getLongFrozenMargin().subtract(unfreezeMargin));
            }
        }

        /**
         * update positionAmt of the AssetBalance
         */
        BigDecimal positionChange = position.getPositionAmt().abs().subtract(oldPositionAmt);
        session.getAssetBalance().setPositionAmt(session.getAssetBalance().getPositionAmt().add(positionChange));

        position.setUpdateTime(System.currentTimeMillis());
    }

    /**
     * update order when matched
     */
    private void updateOrderMatched(TradeSession session, BigDecimal fillPrice, BigDecimal fillQty) {
        SymbolInfo symbolInfo = session.getSymbolInfo();
        Order order = session.getOrder();

        order.setFrozenMargin(order.getFrozenMargin().subtract(session.getUnfreezeMargin()));
        order.setFrozenFee(order.getFrozenFee().subtract(session.getUnfreezeFee()));
        BigDecimal cumQuote = BigDecimalUtil.getVal(fillPrice.multiply(fillQty), symbolInfo.getPriceAssetScale());
        order.setCumQuote(order.getCumQuote().add(cumQuote));
        order.setExecutedQty(order.getExecutedQty().add(fillQty));
        order.setAvgPrice(BigDecimalUtil.getVal(BigDecimalUtil.divide(order.getCumQuote(), order.getExecutedQty()),
            symbolInfo.getPriceAssetScale()));
        order.setFee(order.getFee().add(session.getTrade().getFee()));
        order.setFeeAsset(symbolInfo.getClearAsset());
        order.setCloseProfit(order.getCloseProfit().add(session.getTrade().getCloseProfit()));
        order.setStatus(order.getQuantity().compareTo(order.getExecutedQty()) == 0 ? OrderStatus.FILLED
            : OrderStatus.PARTIALLY_FILLED);
        order.setLeftQty(order.getLeftQty().subtract(fillQty));
        order.setUpdateTime(System.currentTimeMillis());

        /**
         * If the order is filled and ReduceOnly, decrease the ReduceOnlyOrderCount
         */
        if (order.getStatus() == OrderStatus.FILLED && order.getReduceOnly()) {
            session.getPosition().setReduceOnlyOrderCount(session.getPosition().getReduceOnlyOrderCount() - 1);
        }
    }

    /**
     * update AssetBalance when matched
     */
    private void updateBalanceMatched(TradeSession session) {
        AssetBalance assetBalance = session.getAssetBalance();
        Trade trade = session.getTrade();
        Position position = session.getPosition();

        if (position.getMarginType() == MarginType.CROSSED) {
            assetBalance.setFrozenFee(assetBalance.getFrozenFee().subtract(session.getUnfreezeFee()));
            assetBalance.setFee(assetBalance.getFee().add(trade.getFee()));
            assetBalance.setCloseProfit(assetBalance.getCloseProfit().add(trade.getCloseProfit()));
            assetBalance.setBalance(assetBalance.getBalance().add(trade.getCloseProfit()).subtract(trade.getFee()));
            assetBalance.setUpdateTime(System.currentTimeMillis());
        } else {
            position.setIsolatedFrozenFee(position.getIsolatedFrozenFee().subtract(session.getUnfreezeFee()));
            position
                .setIsolatedBalance(position.getIsolatedBalance().add(trade.getCloseProfit()).subtract(trade.getFee()));
        }
    }

    /**
     * update position when canceled
     */
    private void updatePositionCanceled(TradeSession session) {
        Order order = session.getOrder();
        Position position = session.getPosition();

        BigDecimal unfreezeMargin = order.getFrozenMargin();
        BigDecimal unfreezeFee = order.getFrozenFee();
        // set to session
        session.setUnfreezeMargin(unfreezeMargin);
        session.setUnfreezeFee(unfreezeFee);

        /**
         * update frozenAmt and frozenMargin of the position
         */
        if (order.getSide() == OrderSide.SELL) {
            position.setShortFrozenAmt(position.getShortFrozenAmt().subtract(order.getLeftQty()));
            position.setShortFrozenMargin(position.getShortFrozenMargin().subtract(unfreezeMargin));
        } else {
            position.setLongFrozenAmt(position.getLongFrozenAmt().subtract(order.getLeftQty()));
            position.setLongFrozenMargin(position.getLongFrozenMargin().subtract(unfreezeMargin));
        }
        /**
         * If the order is ReduceOnly, decrease the ReduceOnlyOrderCount
         */
        if (order.getReduceOnly()) {
            position.setReduceOnlyOrderCount(position.getReduceOnlyOrderCount() - 1);
        }
        position.setUpdateTime(System.currentTimeMillis());
    }

    /**
     * update order when canceled
     */
    private void updateOrderCanceled(TradeSession session) {
        Order order = session.getOrder();
        order.setFrozenMargin(BigDecimal.ZERO);
        order.setFrozenFee(BigDecimal.ZERO);
        order.setStatus(OrderStatus.CANCELED);
        order.setUpdateTime(System.currentTimeMillis());
    }

    /**
     * update AssetBalance when canceled
     */
    private void updateBalanceCanceled(TradeSession session) {
        AssetBalance assetBalance = session.getAssetBalance();
        Position position = session.getPosition();
        if (position.getMarginType() == MarginType.CROSSED) {
            assetBalance.setFrozenFee(assetBalance.getFrozenFee().subtract(session.getUnfreezeFee()));
            assetBalance.setUpdateTime(System.currentTimeMillis());
        } else {
            position.setIsolatedFrozenFee(position.getIsolatedFrozenFee().subtract(session.getUnfreezeFee()));
        }
    }

    /**
     * save the result when matched
     */
    private void saveMatched(TradeSession session) {
        DataAction orderAction = DataAction.UPDATE;
        DataAction positionAction = session.getPositionAction();
        DataAction orderClientAction = DataAction.NONE;
        DataAction positionClientAction = DataAction.NONE;
        DataAction debtClientAction = DataAction.NONE;

        /**
         * If status of the order is FILLED, remove the order.
         */
        Order order = session.getOrder();
        if (session.getTradeType() == TradeType.STANDARD) {
            if (order.getStatus() == OrderStatus.FILLED) {
                session.getClientEntity().removeOrder(order.getOrderId());
                orderAction = DataAction.DELETE;
            } else {
                /**
                 * Old order loaded in marcher on system restart is not the same object as the order in the client
                 * cache, so if the order has changed, we need to update the order in the client cache.
                 */
                session.getClientEntity().setOrder(order);
            }
        }

        /**
         * If positionAmt = 0 and frozenAmt = 0, remove the position.
         */
        Position position = session.getPosition();
        BigDecimal total =
            position.getPositionAmt().abs().add(position.getShortFrozenAmt().add(position.getLongFrozenAmt()));
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            /**
             * Isolated Margin Mode: transfer isolatedBalance from position to AssetBalance
             */
            if (position.getMarginType() == MarginType.ISOLATED) {
                AssetBalance assetBalance = session.getAssetBalance();
                assetBalance
                    .setIsolatedBalance(assetBalance.getIsolatedBalance().subtract(position.getIsolatedBalance()));
                assetBalance.setBalance(assetBalance.getBalance().add(position.getIsolatedBalance()));
                position.setIsolatedBalance(BigDecimal.ZERO);
            }
            session.getClientEntity().getPositions().remove(position.getSymbol());
            positionAction = DataAction.DELETE;
        }

        /**
         * If the orders of the client is empty, remove the clientId from orderClientIds.
         */
        if (session.getClientEntity().getOrders().isEmpty()) {
            localCache.getOrderClientIds().remove(order.getClientId());
            orderClientAction = DataAction.DELETE;
        }

        /**
         * If the sum of positionAmt is ZERO, remove the clientId from positionClientIds. If the sum of positionAmt is
         * not ZERO, add the clientId to positionClientIds.
         * 
         * debtClientAction: client which no positions and have debts
         */
        if (session.getAssetBalance().getPositionAmt().compareTo(BigDecimal.ZERO) == 0) {
            localCache.getPositionClientIds().remove(order.getClientId());
            positionClientAction = DataAction.DELETE;
            if (session.getAssetBalance().getBalance().compareTo(BigDecimal.ZERO) < 0) {
                localCache.getDebtClientIds().add(order.getClientId());
                debtClientAction = DataAction.INSERT;
            }
        } else {
            if (!localCache.getPositionClientIds().contains(order.getClientId())) {
                localCache.getPositionClientIds().add(order.getClientId());
                positionClientAction = DataAction.INSERT;
            }
            if (localCache.getDebtClientIds().contains(order.getClientId())) {
                localCache.getDebtClientIds().remove(order.getClientId());
                debtClientAction = DataAction.DELETE;
            }
        }

        // realized profit bill
        Bill profitBill = null;
        if (session.getTrade().getCloseProfit().compareTo(BigDecimal.ZERO) != 0) {
            profitBill = Bill.createBill(order.getClientId(), order.getSymbol(), BillType.REALIZED_PNL,
                session.getAssetBalance().getAsset(), session.getTrade().getCloseProfit(), null);
        }
        // fee bill
        Bill feeBill = null;
        if (session.getTrade().getFee().compareTo(BigDecimal.ZERO) != 0) {
            feeBill = Bill.createBill(order.getClientId(), order.getSymbol(), BillType.COMMISSION,
                session.getTrade().getFeeAsset(), session.getTrade().getFee().negate(), null);
        }

        Order cpOrder = orderAction == DataAction.DELETE ? order : beanMapping.clone(order);
        Position cpPosition = positionAction == DataAction.DELETE ? position : beanMapping.clone(position);
        AssetBalance cpAssetBalance = beanMapping.clone(session.getAssetBalance());
        /**
         * set landing to session, the two side of the match write to redis together.
         */
        OrderMatchedLanding landing = new OrderMatchedLanding(session.getTradeType(), cpOrder, orderAction, cpPosition,
            positionAction, cpAssetBalance, session.getBalanceAction(), orderClientAction, positionClientAction,
            session.getTrade(), profitBill, feeBill, debtClientAction);
        session.setLanding(landing);

        /**
         * update the last tradeId of the client
         */
        session.getClientEntity().setLastTradeId(session.getTrade().getTradeId());
    }

    /**
     * save the result when canceled
     */
    private void saveCanceled(TradeSession session) {
        DataAction positionAction = DataAction.UPDATE;
        DataAction orderClientAction = DataAction.NONE;

        /**
         * remove the order.
         */
        Order order = session.getOrder();
        session.getClientEntity().removeOrder(order.getOrderId());

        /**
         * If positionAmt = 0 and frozenAmt = 0, remove the position.
         */
        Position position = session.getPosition();
        BigDecimal total =
            position.getPositionAmt().abs().add(position.getShortFrozenAmt().add(position.getLongFrozenAmt()));
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            /**
             * Isolated Margin Mode: transfer isolatedBalance from position to AssetBalance
             */
            if (position.getMarginType() == MarginType.ISOLATED) {
                AssetBalance assetBalance = session.getAssetBalance();
                assetBalance
                    .setIsolatedBalance(assetBalance.getIsolatedBalance().subtract(position.getIsolatedBalance()));
                assetBalance.setBalance(assetBalance.getBalance().add(position.getIsolatedBalance()));
                position.setIsolatedBalance(BigDecimal.ZERO);
            }
            session.getClientEntity().getPositions().remove(position.getSymbol());
            positionAction = DataAction.DELETE;
        }

        /**
         * If the orders of the client is empty, remove the clientId from orderClientIds.
         */
        if (session.getClientEntity().getOrders().isEmpty()) {
            localCache.getOrderClientIds().remove(order.getClientId());
            orderClientAction = DataAction.DELETE;
        }

        Position cpPosition = positionAction == DataAction.DELETE ? position : beanMapping.clone(position);
        AssetBalance cpAssetBalance = beanMapping.clone(session.getAssetBalance());
        /**
         * save landing to redis
         */
        OrderCanceledLanding landing =
            new OrderCanceledLanding(order, cpPosition, positionAction, cpAssetBalance, orderClientAction);
        redisLanding.orderCanceled(landing);
        /**
         * save landing to mysql
         */
        mySqlLanding.orderCanceled(landing);
        /**
         * private publish
         */
        privatePublish.publishComplex(
            new ComplexEntity(order, Collections.singletonList(cpAssetBalance), cpPosition, null, null));
    }

}
