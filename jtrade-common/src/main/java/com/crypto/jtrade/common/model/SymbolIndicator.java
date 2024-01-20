package com.crypto.jtrade.common.model;

import java.math.BigDecimal;

import lombok.Data;

/**
 * Symbol related indicators
 *
 * @author 0xWill
 **/
@Data
public class SymbolIndicator {

    private BigDecimal indexPrice;

    private BigDecimal markPrice;

    private BigDecimal fundingRate;
}
