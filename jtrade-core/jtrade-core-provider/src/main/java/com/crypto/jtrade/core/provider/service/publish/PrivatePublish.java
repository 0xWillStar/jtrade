package com.crypto.jtrade.core.provider.service.publish;

import com.crypto.jtrade.common.model.ComplexEntity;
import com.crypto.jtrade.core.provider.model.landing.FundingFeeLanding;

/**
 * private data publish service
 *
 * @author 0xWill
 **/
public interface PrivatePublish {

    /**
     * publish complex entity
     */
    void publishComplex(ComplexEntity complexEntity);

    /**
     * set funding fee
     */
    void setFundingFee(FundingFeeLanding fundingFeeLanding);

}
