package com.crypto.jtrade.core.provider.service.rule.impl;

import java.math.BigDecimal;
import java.util.List;

import com.crypto.jtrade.common.constants.MatchRole;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.core.api.model.AdjustPositionMarginRequest;
import com.crypto.jtrade.core.api.model.CancelOrderRequest;
import com.crypto.jtrade.core.api.model.ClientSettingRequest;
import com.crypto.jtrade.core.api.model.DepositRequest;
import com.crypto.jtrade.core.api.model.FundingRateRequest;
import com.crypto.jtrade.core.api.model.OTCRequest;
import com.crypto.jtrade.core.api.model.PlaceOrderRequest;
import com.crypto.jtrade.core.api.model.WithdrawRequest;
import com.crypto.jtrade.core.provider.model.session.OrderSession;
import com.crypto.jtrade.core.provider.model.session.TradeSession;
import com.crypto.jtrade.core.provider.service.rule.TradeRule;

/**
 * abstract trade rule, default implement TradeRule
 *
 * @author 0xWill
 **/
public abstract class AbstractTradeRule implements TradeRule {

    /**
     * set client setting
     */
    @Override
    public void setClientSetting(Long requestId, ClientSettingRequest request) {

    }

    /**
     * deposit
     */
    @Override
    public void deposit(Long requestId, DepositRequest request) {

    }

    /**
     * withdraw
     */
    @Override
    public void withdraw(Long requestId, WithdrawRequest request) {

    }

    /**
     * place order
     */
    @Override
    public void placeOrder(PlaceOrderRequest request, OrderSession session) {

    }

    /**
     * cancel order
     */
    @Override
    public void cancelOrder(CancelOrderRequest request, OrderSession session) {

    }

    /**
     * order matched
     */
    @Override
    public void orderMatched(TradeSession session, BigDecimal fillPrice, BigDecimal fillQty, MatchRole matchRole,
        Long tradeId) {

    }

    /**
     * order canceled
     */
    @Override
    public void orderCanceled(TradeSession session) {

    }

    /**
     * set funding rate
     */
    @Override
    public void setFundingRate(Long requestId, List<FundingRateRequest> request) {

    }

    /**
     * OTC trade
     */
    @Override
    public void otcTrade(Long requestId, OTCRequest request) {

    }

    /**
     * adjust position margin
     */
    @Override
    public void adjustPositionMargin(Long requestId, AdjustPositionMarginRequest request) {

    }

    /**
     * trigger secondary order
     */
    @Override
    public void triggerSecondaryOrder(Long requestId, Order order, OrderSession session) {

    }

    /**
     * deduct collateral asset
     */
    @Override
    public void deductCollateral(Long requestId, String clientId) {

    }

}
