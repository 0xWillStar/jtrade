package com.crypto.jtrade.core.provider.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.core.api.TradeApi;
import com.crypto.jtrade.core.api.model.AdjustPositionMarginRequest;
import com.crypto.jtrade.core.api.model.AssetInfoRequest;
import com.crypto.jtrade.core.api.model.CancelOrderRequest;
import com.crypto.jtrade.core.api.model.ClientFeeRateRequest;
import com.crypto.jtrade.core.api.model.ClientSettingRequest;
import com.crypto.jtrade.core.api.model.DepositRequest;
import com.crypto.jtrade.core.api.model.EmptyRequest;
import com.crypto.jtrade.core.api.model.FundingRateRequest;
import com.crypto.jtrade.core.api.model.LiquidationCancelOrderRequest;
import com.crypto.jtrade.core.api.model.MarkPriceRequest;
import com.crypto.jtrade.core.api.model.OTCRequest;
import com.crypto.jtrade.core.api.model.PlaceOrderRequest;
import com.crypto.jtrade.core.api.model.PlaceOrderResponse;
import com.crypto.jtrade.core.api.model.SymbolIndicatorRequest;
import com.crypto.jtrade.core.api.model.SymbolInfoRequest;
import com.crypto.jtrade.core.api.model.SystemParameterRequest;
import com.crypto.jtrade.core.api.model.TradeAuthorityRequest;
import com.crypto.jtrade.core.api.model.WithdrawRequest;
import com.crypto.jtrade.core.provider.service.trade.TradeService;

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
    public BaseResponse emptyCommand(EmptyRequest request) {
        return tradeService.emptyCommand(request);
    }

    @Override
    public BaseResponse setSystemParameter(SystemParameterRequest request) {
        return tradeService.setSystemParameter(request);
    }

    @Override
    public BaseResponse setSymbolInfo(SymbolInfoRequest request) {
        return tradeService.setSymbolInfo(request);
    }

    @Override
    public BaseResponse setSymbolIndicator(SymbolIndicatorRequest request) {
        return tradeService.setSymbolIndicator(request);
    }

    @Override
    public BaseResponse setAssetInfo(AssetInfoRequest request) {
        return tradeService.setAssetInfo(request);
    }

    @Override
    public BaseResponse setFundingRate(List<FundingRateRequest> request) {
        return tradeService.setFundingRate(request);
    }

    @Override
    public BaseResponse setMarkPrice(List<MarkPriceRequest> request) {
        return tradeService.setMarkPrice(request);
    }

    @Override
    public BaseResponse setClientFeeRate(List<ClientFeeRateRequest> request) {
        return tradeService.setClientFeeRate(request);
    }

    @Override
    public BaseResponse setClientTradeAuthority(List<TradeAuthorityRequest> request) {
        return tradeService.setClientTradeAuthority(request);
    }

    @Override
    public BaseResponse setClientSetting(ClientSettingRequest request) {
        return tradeService.setClientSetting(request);
    }

    @Override
    public BaseResponse deposit(DepositRequest request) {
        return tradeService.deposit(request);
    }

    @Override
    public BaseResponse withdraw(WithdrawRequest request) {
        return tradeService.withdraw(request);
    }

    @Override
    public BaseResponse<PlaceOrderResponse> placeOrder(PlaceOrderRequest request) {
        return tradeService.placeOrder(request);
    }

    @Override
    public BaseResponse cancelOrder(CancelOrderRequest request) {
        return tradeService.cancelOrder(request);
    }

    @Override
    public BaseResponse liquidationCancelOrder(LiquidationCancelOrderRequest request) {
        return tradeService.liquidationCancelOrder(request);
    }

    @Override
    public BaseResponse otcTrade(OTCRequest request) {
        return tradeService.otcTrade(request);
    }

    @Override
    public BaseResponse adjustPositionMargin(AdjustPositionMarginRequest request) {
        return tradeService.adjustPositionMargin(request);
    }

    @Override
    public BaseResponse<PlaceOrderResponse> placeOTOOrder(List<PlaceOrderRequest> request) {
        return tradeService.placeOTOOrder(request);
    }

}
