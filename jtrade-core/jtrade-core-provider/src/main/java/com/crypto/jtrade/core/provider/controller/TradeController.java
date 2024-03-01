package com.crypto.jtrade.core.provider.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.core.api.TradeApi;
import com.crypto.jtrade.core.api.model.*;
import com.crypto.jtrade.core.provider.service.trade.TradeCommand;

/**
 * trade api controller
 *
 * @author 0xWill
 **/
@RestController
public class TradeController implements TradeApi {

    @Autowired
    private TradeCommand tradeCommand;

    @Override
    public BaseResponse emptyCommand(EmptyRequest request) {
        return tradeCommand.emptyCommand(request);
    }

    @Override
    public BaseResponse setSystemParameter(SystemParameterRequest request) {
        return tradeCommand.setSystemParameter(request);
    }

    @Override
    public BaseResponse setSymbolInfo(SymbolInfoRequest request) {
        return tradeCommand.setSymbolInfo(request);
    }

    @Override
    public BaseResponse setSymbolIndicator(SymbolIndicatorRequest request) {
        return tradeCommand.setSymbolIndicator(request);
    }

    @Override
    public BaseResponse setAssetInfo(AssetInfoRequest request) {
        return tradeCommand.setAssetInfo(request);
    }

    @Override
    public BaseResponse setFundingRate(List<FundingRateRequest> request) {
        return tradeCommand.setFundingRate(request);
    }

    @Override
    public BaseResponse setMarkPrice(List<MarkPriceRequest> request) {
        return tradeCommand.setMarkPrice(request);
    }

    @Override
    public BaseResponse setClientFeeRate(List<ClientFeeRateRequest> request) {
        return tradeCommand.setClientFeeRate(request);
    }

    @Override
    public BaseResponse setClientTradeAuthority(List<TradeAuthorityRequest> request) {
        return tradeCommand.setClientTradeAuthority(request);
    }

    @Override
    public BaseResponse setClientSetting(ClientSettingRequest request) {
        return tradeCommand.setClientSetting(request);
    }

    @Override
    public BaseResponse deposit(DepositRequest request) {
        return tradeCommand.deposit(request);
    }

    @Override
    public BaseResponse withdraw(WithdrawRequest request) {
        return tradeCommand.withdraw(request);
    }

    @Override
    public BaseResponse<PlaceOrderResponse> placeOrder(PlaceOrderRequest request) {
        return tradeCommand.placeOrder(request);
    }

    @Override
    public BaseResponse cancelOrder(CancelOrderRequest request) {
        return tradeCommand.cancelOrder(request);
    }

    @Override
    public BaseResponse liquidationCancelOrder(LiquidationCancelOrderRequest request) {
        return tradeCommand.liquidationCancelOrder(request);
    }

    @Override
    public BaseResponse otcTrade(OTCRequest request) {
        return tradeCommand.otcTrade(request);
    }

    @Override
    public BaseResponse adjustPositionMargin(AdjustPositionMarginRequest request) {
        return tradeCommand.adjustPositionMargin(request);
    }

    @Override
    public BaseResponse<PlaceOrderResponse> placeOTOOrder(List<PlaceOrderRequest> request) {
        return tradeCommand.placeOTOOrder(request);
    }

}
