package com.crypto.jtrade.core.provider.service.rule;

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

/**
 * trade rule
 *
 * @author 0xWill
 **/
public interface TradeRule {

    /**
     * get the sequence
     */
    int getSequence();

    /**
     * product type to be used
     */
    long getUsedProductType();

    /**
     * command to be used
     */
    long getUsedCommand();

    /**
     * set client setting
     */
    void setClientSetting(Long requestId, ClientSettingRequest request);

    /**
     * deposit
     */
    void deposit(Long requestId, DepositRequest request);

    /**
     * withdraw
     */
    void withdraw(Long requestId, WithdrawRequest request);

    /**
     * place order
     */
    void placeOrder(PlaceOrderRequest request, OrderSession session);

    /**
     * cancel order
     */
    void cancelOrder(CancelOrderRequest request, OrderSession session);

    /**
     * order matched
     */
    void orderMatched(TradeSession session, BigDecimal fillPrice, BigDecimal fillQty, MatchRole matchRole,
        Long tradeId);

    /**
     * order canceled
     */
    void orderCanceled(TradeSession session);

    /**
     * set funding rate
     */
    void setFundingRate(Long requestId, List<FundingRateRequest> request);

    /**
     * OTC trade
     */
    void otcTrade(Long requestId, OTCRequest request);

    /**
     * adjust position margin
     */
    void adjustPositionMargin(Long requestId, AdjustPositionMarginRequest request);

    /**
     * trigger secondary order
     */
    void triggerSecondaryOrder(Long requestId, Order order, OrderSession session);

    /**
     * deduct collateral asset
     */
    void deductCollateral(Long requestId, String clientId);

}
