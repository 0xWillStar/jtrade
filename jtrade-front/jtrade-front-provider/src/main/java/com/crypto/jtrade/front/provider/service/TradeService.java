package com.crypto.jtrade.front.provider.service;

import java.util.List;

import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.model.Bill;
import com.crypto.jtrade.common.model.ClientSetting;
import com.crypto.jtrade.common.model.FeeRate;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.Trade;
import com.crypto.jtrade.core.api.model.AdjustPositionMarginRequest;
import com.crypto.jtrade.core.api.model.CancelOrderRequest;
import com.crypto.jtrade.core.api.model.ClientSettingRequest;
import com.crypto.jtrade.core.api.model.PlaceOrderRequest;
import com.crypto.jtrade.core.api.model.PlaceOrderResponse;
import com.crypto.jtrade.core.api.model.WithdrawRequest;
import com.crypto.jtrade.front.api.model.LoginRequest;
import com.crypto.jtrade.front.api.model.LoginResponse;

/**
 * trade service
 *
 * @author 0xWill
 **/
public interface TradeService {

    LoginResponse login(String address, String timestamp, String signature, String host, LoginRequest request);

    BaseResponse setClientSetting(ClientSettingRequest request);

    BaseResponse withdraw(WithdrawRequest request);

    BaseResponse<PlaceOrderResponse> placeOrder(PlaceOrderRequest request);

    BaseResponse<PlaceOrderResponse> placeOTOOrder(List<PlaceOrderRequest> request);

    BaseResponse cancelOrder(CancelOrderRequest request);

    BaseResponse adjustPositionMargin(AdjustPositionMarginRequest request);

    List<Order> getOpenOrders(String clientId, String symbol);

    List<Position> getPositions(String clientId, String symbol);

    List<AssetBalance> getBalances(String clientId, String asset);

    Order getOrder(String clientId, String symbol, String clientOrderId, Long orderId);

    List<Order> getHistoryOrders(String clientId, String symbol, Long orderId, Long startTime, Long endTime,
        Long limit);

    List<Trade> getHistoryTrades(String clientId, String symbol, Long fromId, Long startTime, Long endTime, Long limit);

    List<Bill> getBills(String clientId, String symbol, Long startTime, Long endTime, Long limit);

    FeeRate getFeeRate(String clientId, String symbol);

    ClientSetting getClientSetting(String clientId, String symbol);

}
