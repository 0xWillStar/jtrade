package com.crypto.jtrade.core.api;

import java.util.List;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.crypto.jtrade.common.model.BaseResponse;
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

import feign.Headers;
import feign.RequestLine;

/**
 * define trade api
 *
 * @author 0xWill
 **/
@Headers("Content-Type: application/json")
public interface TradeApi {

    @PostMapping(value = "/v1/trade/emptyCommand")
    @RequestLine("POST /v1/trade/emptyCommand")
    BaseResponse emptyCommand(@RequestBody EmptyRequest request);

    @PostMapping(value = "/v1/trade/setSystemParameter")
    @RequestLine("POST /v1/trade/setSystemParameter")
    BaseResponse setSystemParameter(@RequestBody SystemParameterRequest request);

    @PostMapping(value = "/v1/trade/setSymbolInfo")
    @RequestLine("POST /v1/trade/setSymbolInfo")
    BaseResponse setSymbolInfo(@RequestBody SymbolInfoRequest request);

    @PostMapping(value = "/v1/trade/setSymbolIndicator")
    @RequestLine("POST /v1/trade/setSymbolIndicator")
    BaseResponse setSymbolIndicator(@RequestBody SymbolIndicatorRequest request);

    @PostMapping(value = "/v1/trade/setAssetInfo")
    @RequestLine("POST /v1/trade/setAssetInfo")
    BaseResponse setAssetInfo(@RequestBody AssetInfoRequest request);

    @PostMapping(value = "/v1/trade/setFundingRate")
    @RequestLine("POST /v1/trade/setFundingRate")
    BaseResponse setFundingRate(@RequestBody List<FundingRateRequest> request);

    @PostMapping(value = "/v1/trade/setMarkPrice")
    @RequestLine("POST /v1/trade/setMarkPrice")
    BaseResponse setMarkPrice(@RequestBody List<MarkPriceRequest> request);

    @PostMapping(value = "/v1/trade/setClientFeeRate")
    @RequestLine("POST /v1/trade/setClientFeeRate")
    BaseResponse setClientFeeRate(@RequestBody List<ClientFeeRateRequest> request);

    @PostMapping(value = "/v1/trade/setClientTradeAuthority")
    @RequestLine("POST /v1/trade/setClientTradeAuthority")
    BaseResponse setClientTradeAuthority(@RequestBody List<TradeAuthorityRequest> request);

    @PostMapping(value = "/v1/trade/setClientSetting")
    @RequestLine("POST /v1/trade/setClientSetting")
    BaseResponse setClientSetting(@RequestBody ClientSettingRequest request);

    @PostMapping(value = "/v1/trade/deposit")
    @RequestLine("POST /v1/trade/deposit")
    BaseResponse deposit(@RequestBody DepositRequest request);

    @PostMapping(value = "/v1/trade/withdraw")
    @RequestLine("POST /v1/trade/withdraw")
    BaseResponse withdraw(@RequestBody WithdrawRequest request);

    @PostMapping(value = "/v1/trade/placeOrder")
    @RequestLine("POST /v1/trade/placeOrder")
    BaseResponse<PlaceOrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request);

    @PostMapping(value = "/v1/trade/cancelOrder")
    @RequestLine("POST /v1/trade/cancelOrder")
    BaseResponse cancelOrder(@RequestBody CancelOrderRequest request);

    @PostMapping(value = "/v1/trade/liquidationCancelOrder")
    @RequestLine("POST /v1/trade/liquidationCancelOrder")
    BaseResponse liquidationCancelOrder(@RequestBody LiquidationCancelOrderRequest request);

    @PostMapping(value = "/v1/trade/otcTrade")
    @RequestLine("POST /v1/trade/otcTrade")
    BaseResponse otcTrade(@RequestBody OTCRequest request);

    @PostMapping(value = "/v1/trade/adjustPositionMargin")
    @RequestLine("POST /v1/trade/adjustPositionMargin")
    BaseResponse adjustPositionMargin(@RequestBody AdjustPositionMarginRequest request);

    @PostMapping(value = "/v1/trade/placeOTOOrder")
    @RequestLine("POST /v1/trade/placeOTOOrder")
    BaseResponse<PlaceOrderResponse> placeOTOOrder(@Valid @RequestBody List<PlaceOrderRequest> request);

}
