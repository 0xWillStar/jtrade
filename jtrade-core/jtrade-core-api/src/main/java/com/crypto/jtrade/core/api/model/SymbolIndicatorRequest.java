package com.crypto.jtrade.core.api.model;

import com.crypto.jtrade.common.constants.IndicatorType;

import lombok.Data;

/**
 * symbol indicator request
 *
 * @author 0xWill
 */
@Data
public class SymbolIndicatorRequest {

    private String symbol;

    private IndicatorType indicatorType;

    private String value;
}
