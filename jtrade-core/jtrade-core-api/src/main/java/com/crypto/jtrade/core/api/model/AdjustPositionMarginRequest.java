package com.crypto.jtrade.core.api.model;

import java.math.BigDecimal;

import com.crypto.jtrade.common.constants.PositionSide;

import lombok.Data;

/**
 * adjust position margin request
 *
 * @author 0xWill
 */
@Data
public class AdjustPositionMarginRequest extends BaseCoreRequest {

    private String symbol;

    private PositionSide positionSide = PositionSide.NET;

    /**
     * Positive value means increase, negative value means decrease.
     */
    private BigDecimal amount;

}
