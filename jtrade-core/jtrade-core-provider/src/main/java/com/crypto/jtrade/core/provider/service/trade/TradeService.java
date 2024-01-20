package com.crypto.jtrade.core.provider.service.trade;

import java.util.List;

import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.model.Order;
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

/**
 * Trade service
 *
 * @author 0xWill
 **/
public interface TradeService {

    /**
     * empty command
     */
    BaseResponse emptyCommand(EmptyRequest request);

    /**
     * set system parameter
     */
    BaseResponse setSystemParameter(SystemParameterRequest request);

    /**
     * set symbol info
     */
    BaseResponse setSymbolInfo(SymbolInfoRequest request);

    /**
     * set symbol indicator
     */
    BaseResponse setSymbolIndicator(SymbolIndicatorRequest request);

    /**
     * set asset info
     */
    BaseResponse setAssetInfo(AssetInfoRequest request);

    /**
     * set funding rate
     */
    BaseResponse setFundingRate(List<FundingRateRequest> request);

    /**
     * set mark price
     */
    BaseResponse setMarkPrice(List<MarkPriceRequest> request);

    /**
     * set fee rate by client
     */
    BaseResponse setClientFeeRate(List<ClientFeeRateRequest> request);

    /**
     * set trade authority by client
     */
    BaseResponse setClientTradeAuthority(List<TradeAuthorityRequest> request);

    /**
     * set client setting
     */
    BaseResponse setClientSetting(ClientSettingRequest request);

    /**
     * deposit
     */
    BaseResponse deposit(DepositRequest request);

    /**
     * withdraw
     */
    BaseResponse withdraw(WithdrawRequest request);

    /**
     * place order
     */
    BaseResponse<PlaceOrderResponse> placeOrder(PlaceOrderRequest request);

    /**
     * place OTO order
     */
    BaseResponse<PlaceOrderResponse> placeOTOOrder(List<PlaceOrderRequest> request);

    /**
     * place order when stop order is triggered
     */
    BaseResponse stopTriggeredPlaceOrder(Order request);

    /**
     * cancel order
     */
    BaseResponse cancelOrder(CancelOrderRequest request);

    /**
     * cancel order when stop order is rejected
     */
    BaseResponse stopRejectedCancelOrder(Order request);

    /**
     * cancel order when liquidation
     */
    BaseResponse liquidationCancelOrder(LiquidationCancelOrderRequest request);

    /**
     * trigger secondary order
     */
    BaseResponse triggerSecondaryOrder(Order request);

    /**
     * OTC trade
     */
    BaseResponse otcTrade(OTCRequest request);

    /**
     * adjust position margin
     */
    BaseResponse adjustPositionMargin(AdjustPositionMarginRequest request);

    /**
     * deduct collateral assets
     */
    BaseResponse deductCollateralAssets(String clientId);

}
