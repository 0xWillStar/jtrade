package com.crypto.jtrade.front.provider.controller;

import static com.crypto.jtrade.common.constants.Constants.DOT_STR;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.model.Bill;
import com.crypto.jtrade.common.model.ClientSetting;
import com.crypto.jtrade.common.model.FeeRate;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.Trade;
import com.crypto.jtrade.common.util.ResponseHelper;
import com.crypto.jtrade.core.api.model.AdjustPositionMarginRequest;
import com.crypto.jtrade.core.api.model.CancelOrderRequest;
import com.crypto.jtrade.core.api.model.ClientSettingRequest;
import com.crypto.jtrade.core.api.model.PlaceOrderRequest;
import com.crypto.jtrade.core.api.model.PlaceOrderResponse;
import com.crypto.jtrade.core.api.model.WithdrawRequest;
import com.crypto.jtrade.front.api.TradeApi;
import com.crypto.jtrade.front.api.model.LoginRequest;
import com.crypto.jtrade.front.api.model.LoginResponse;
import com.crypto.jtrade.front.provider.constants.Constants;
import com.crypto.jtrade.front.provider.service.TradeService;
import com.crypto.jtrade.front.provider.util.ContextUtil;

/**
 * trade api controller
 *
 * @author 0xWill
 **/
@RestController
public class TradeController implements TradeApi {

    @Autowired
    private TradeService tradeService;

    @Override
    public BaseResponse<LoginResponse> login(LoginRequest request, HttpServletRequest httpRequest) {
        String address = httpRequest.getHeader(Constants.HEADER_ADDRESS);
        String timestamp = httpRequest.getHeader(Constants.HEADER_TIMESTAMP);
        String signature = httpRequest.getHeader(Constants.HEADER_SIGNATURE);
        String host = getSignHost(httpRequest);
        return ResponseHelper.success(tradeService.login(address, timestamp, signature, host, request));
    }

    @Override
    public BaseResponse setClientSetting(ClientSettingRequest request) {
        request.setClientId(ContextUtil.getAccountByContext());
        return tradeService.setClientSetting(request);
    }

    @Override
    public BaseResponse withdraw(WithdrawRequest request) {
        request.setClientId(ContextUtil.getAccountByContext());
        return tradeService.withdraw(request);
    }

    @Override
    public BaseResponse<PlaceOrderResponse> placeOrder(PlaceOrderRequest request) {
        request.setClientId(ContextUtil.getAccountByContext());
        return tradeService.placeOrder(request);
    }

    @Override
    public BaseResponse<PlaceOrderResponse> placeOTOOrder(List<PlaceOrderRequest> request) {
        String clientId = ContextUtil.getAccountByContext();
        request.forEach(order -> order.setClientId(clientId));
        return tradeService.placeOTOOrder(request);
    }

    @Override
    public BaseResponse cancelOrder(CancelOrderRequest request) {
        request.setClientId(ContextUtil.getAccountByContext());
        return tradeService.cancelOrder(request);
    }

    @Override
    public BaseResponse adjustPositionMargin(AdjustPositionMarginRequest request) {
        request.setClientId(ContextUtil.getAccountByContext());
        return tradeService.adjustPositionMargin(request);
    }

    @Override
    public BaseResponse<List<Order>> getOpenOrders(String symbol) {
        String clientId = ContextUtil.getAccountByContext();
        return ResponseHelper.success(tradeService.getOpenOrders(clientId, symbol));
    }

    @Override
    public BaseResponse<List<Position>> getPositions(String symbol) {
        String clientId = ContextUtil.getAccountByContext();
        return ResponseHelper.success(tradeService.getPositions(clientId, symbol));
    }

    @Override
    public BaseResponse<List<AssetBalance>> getBalances(String asset) {
        String clientId = ContextUtil.getAccountByContext();
        return ResponseHelper.success(tradeService.getBalances(clientId, asset));
    }

    @Override
    public BaseResponse<Order> getOrder(String symbol, String clientOrderId, Long orderId) {
        String clientId = ContextUtil.getAccountByContext();
        return ResponseHelper.success(tradeService.getOrder(clientId, symbol, clientOrderId, orderId));
    }

    @Override
    public BaseResponse<List<Order>> getHistoryOrders(String symbol, Long orderId, Long startTime, Long endTime,
        Long limit) {
        String clientId = ContextUtil.getAccountByContext();
        return ResponseHelper
            .success(tradeService.getHistoryOrders(clientId, symbol, orderId, startTime, endTime, limit));
    }

    @Override
    public BaseResponse<List<Trade>> getHistoryTrades(String symbol, Long fromId, Long startTime, Long endTime,
        Long limit) {
        String clientId = ContextUtil.getAccountByContext();
        return ResponseHelper
            .success(tradeService.getHistoryTrades(clientId, symbol, fromId, startTime, endTime, limit));
    }

    @Override
    public BaseResponse<List<Bill>> getBills(String symbol, String billType, Long startTime, Long endTime, Long limit) {
        String clientId = ContextUtil.getAccountByContext();
        return ResponseHelper.success(tradeService.getBills(clientId, symbol, startTime, endTime, limit));
    }

    @Override
    public BaseResponse<FeeRate> getFeeRate(String symbol) {
        String clientId = ContextUtil.getAccountByContext();
        return ResponseHelper.success(tradeService.getFeeRate(clientId, symbol));
    }

    @Override
    public BaseResponse<ClientSetting> getClientSetting(String symbol) {
        String clientId = ContextUtil.getAccountByContext();
        return ResponseHelper.success(tradeService.getClientSetting(clientId, symbol));
    }

    private String getSignHost(HttpServletRequest httpRequest) {
        String host = httpRequest.getServerName();
        if (StringUtils.countMatches(host, DOT_STR) > 1) {
            host = StringUtils.substringAfter(host, DOT_STR);
        }
        return String.format("https://%s", host);
    }
}
