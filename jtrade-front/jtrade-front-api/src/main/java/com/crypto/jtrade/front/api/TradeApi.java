package com.crypto.jtrade.front.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.crypto.jtrade.common.model.*;
import com.crypto.jtrade.core.api.model.*;
import com.crypto.jtrade.front.api.model.LoginRequest;
import com.crypto.jtrade.front.api.model.LoginResponse;

/**
 * define trade api
 *
 * @author 0xWill
 **/
public interface TradeApi {

    @PostMapping("/v1/trade/login")
    BaseResponse<LoginResponse> login(@RequestBody(required = false) LoginRequest request,
        HttpServletRequest httpRequest);

    @PostMapping("/v1/trade/setClientSetting")
    BaseResponse setClientSetting(@RequestBody ClientSettingRequest request);

    @PostMapping("/v1/trade/withdraw")
    BaseResponse withdraw(@RequestBody WithdrawRequest request);

    @PostMapping("/v1/trade/placeOrder")
    BaseResponse<PlaceOrderResponse> placeOrder(@Validated @RequestBody PlaceOrderRequest request);

    @PostMapping(value = "/v1/trade/placeOTOOrder")
    BaseResponse<PlaceOrderResponse> placeOTOOrder(@Validated @RequestBody List<PlaceOrderRequest> request);

    @PostMapping("/v1/trade/cancelOrder")
    BaseResponse cancelOrder(@RequestBody CancelOrderRequest request);

    @PostMapping("/v1/trade/adjustPositionMargin")
    BaseResponse adjustPositionMargin(@RequestBody AdjustPositionMarginRequest request);

    @GetMapping("/v1/trade/openOrders")
    BaseResponse<List<Order>> getOpenOrders(@RequestParam(value = "symbol", required = false) String symbol);

    @GetMapping("/v1/trade/positions")
    BaseResponse<List<Position>> getPositions(@RequestParam(value = "symbol", required = false) String symbol);

    @GetMapping("/v1/trade/balances")
    BaseResponse<List<AssetBalance>> getBalances(@RequestParam(value = "asset", required = false) String asset);

    @GetMapping("/v1/trade/order")
    BaseResponse<Order> getOrder(@RequestParam("symbol") String symbol,
        @RequestParam(value = "clientOrderId", required = false) String clientOrderId,
        @RequestParam(value = "orderId", required = false) Long orderId);

    @GetMapping("/v1/trade/historyOrders")
    BaseResponse<List<Order>> getHistoryOrders(@RequestParam(value = "symbol", required = false) String symbol,
        @RequestParam(value = "orderId", required = false) Long orderId,
        @RequestParam(value = "startTime", required = false) Long startTime,
        @RequestParam(value = "endTime", required = false) Long endTime,
        @RequestParam(value = "limit", required = false, defaultValue = "100") Long limit);

    @GetMapping("/v1/trade/historyTrades")
    BaseResponse<List<Trade>> getHistoryTrades(@RequestParam(value = "symbol", required = false) String symbol,
        @RequestParam(value = "fromId", required = false) Long fromId,
        @RequestParam(value = "startTime", required = false) Long startTime,
        @RequestParam(value = "endTime", required = false) Long endTime,
        @RequestParam(value = "limit", required = false, defaultValue = "100") Long limit);

    @GetMapping("/v1/trade/bills")
    BaseResponse<List<Bill>> getBills(@RequestParam(value = "symbol", required = false) String symbol,
        @RequestParam(value = "billType", required = false) String billType,
        @RequestParam(value = "startTime", required = false) Long startTime,
        @RequestParam(value = "endTime", required = false) Long endTime,
        @RequestParam(value = "limit", required = false, defaultValue = "100") Long limit);

    @GetMapping("/v1/trade/feeRate")
    BaseResponse<FeeRate> getFeeRate(@RequestParam(value = "symbol", required = false) String symbol);

    @GetMapping("/v1/trade/clientSetting")
    BaseResponse<ClientSetting> getClientSetting(@RequestParam("symbol") String symbol);

}
