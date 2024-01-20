package com.crypto.jtrade.core.api.model;

import java.math.BigDecimal;

import com.crypto.jtrade.common.model.FundingRate;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * funding rate request
 *
 * @author 0xWill
 */
@Data
@NoArgsConstructor
public class FundingRateRequest extends FundingRate {

    public FundingRateRequest(String symbol, BigDecimal fundingRate, Long time) {
        setSymbol(symbol);
        setFundingRate(fundingRate);
        setTime(time);
    }

}
