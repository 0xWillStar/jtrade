package com.crypto.jtrade.core.provider.service.publish;

import com.crypto.jtrade.common.model.Depth;
import com.crypto.jtrade.common.model.FundingRate;
import com.crypto.jtrade.common.model.MarkPrice;
import com.crypto.jtrade.common.model.Trade;

/**
 * public data publish service
 *
 * @author 0xWill
 **/
public interface PublicPublish {

    /**
     * publish depth
     */
    void publishDepth(Depth depth);

    /**
     * publish trade
     */
    void publishTrade(Trade trade);

    /**
     * publish mark price
     */
    void publishMarkPrice(MarkPrice markPrice);

    /**
     * publish funding rate
     */
    void publishFundingRate(FundingRate fundingRate);
}
