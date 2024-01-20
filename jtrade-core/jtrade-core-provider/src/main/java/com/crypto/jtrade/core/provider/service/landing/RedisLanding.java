package com.crypto.jtrade.core.provider.service.landing;

import com.crypto.jtrade.core.provider.model.landing.AdjustPositionMarginLanding;
import com.crypto.jtrade.core.provider.model.landing.AssetInfoLanding;
import com.crypto.jtrade.core.provider.model.landing.CancelOrderLanding;
import com.crypto.jtrade.core.provider.model.landing.ClientFeeRateLanding;
import com.crypto.jtrade.core.provider.model.landing.ClientSettingLanding;
import com.crypto.jtrade.core.provider.model.landing.ClientTradeAuthorityLanding;
import com.crypto.jtrade.core.provider.model.landing.DeductCollateralLanding;
import com.crypto.jtrade.core.provider.model.landing.DepositLanding;
import com.crypto.jtrade.core.provider.model.landing.FundingFeeLanding;
import com.crypto.jtrade.core.provider.model.landing.MatchedLandings;
import com.crypto.jtrade.core.provider.model.landing.OrderCanceledLanding;
import com.crypto.jtrade.core.provider.model.landing.PlaceOrderLanding;
import com.crypto.jtrade.core.provider.model.landing.SymbolInfoLanding;
import com.crypto.jtrade.core.provider.model.landing.SystemParameterLanding;
import com.crypto.jtrade.core.provider.model.landing.WithdrawLanding;

/**
 * save data to redis asynchronously
 *
 * @author 0xWill
 **/
public interface RedisLanding {

    /**
     * set system parameter
     */
    void setSystemParameter(SystemParameterLanding landing);

    /**
     * set symbol info
     */
    void setSymbolInfo(SymbolInfoLanding landing);

    /**
     * set asset info
     */
    void setAssetInfo(AssetInfoLanding landing);

    /**
     * set client fee rate
     */
    void setClientFeeRate(ClientFeeRateLanding landing);

    /**
     * set client trade authority
     */
    void setClientTradeAuthority(ClientTradeAuthorityLanding landing);

    /**
     * set client setting
     */
    void setClientSetting(ClientSettingLanding landing);

    /**
     * deposit
     */
    void deposit(DepositLanding landing);

    /**
     * withdraw
     */
    void withdraw(WithdrawLanding landing);

    /**
     * place order
     */
    void placeOrder(PlaceOrderLanding landing);

    /**
     * cancel order
     */
    void cancelOrder(CancelOrderLanding landing);

    /**
     * order matched
     */
    void orderMatched(MatchedLandings landing);

    /**
     * order canceled
     */
    void orderCanceled(OrderCanceledLanding landing);

    /**
     * set funding fee
     */
    void setFundingFee(FundingFeeLanding landing);

    /**
     * adjust position margin
     */
    void adjustPositionMargin(AdjustPositionMarginLanding landing);

    /**
     * deduct collateral
     */
    void deductCollateral(DeductCollateralLanding landing);

}
