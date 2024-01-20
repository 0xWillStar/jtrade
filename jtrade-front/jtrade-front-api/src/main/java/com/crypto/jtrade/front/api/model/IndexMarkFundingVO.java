package com.crypto.jtrade.front.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * index price、mark price、funding rate VO
 *
 * @author 0xWill
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexMarkFundingVO {

    private String symbol;

    private String indexPrice;

    private Long indexPriceTime;

    private String markPrice;

    private Long markPriceTime;

    private String fundingRate;

    private Long fundingRateTime;

}
