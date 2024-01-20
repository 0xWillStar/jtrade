package com.crypto.jtrade.core.api.model;

import java.math.BigDecimal;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import com.crypto.jtrade.common.constants.OTOOrderType;
import com.crypto.jtrade.common.constants.OrderSide;
import com.crypto.jtrade.common.constants.OrderType;
import com.crypto.jtrade.common.constants.PositionSide;
import com.crypto.jtrade.common.constants.TimeInForce;
import com.crypto.jtrade.common.constants.WorkingType;

import lombok.Data;

/**
 * place order request
 *
 * @author 0xWill
 */
@Data
public class PlaceOrderRequest extends BaseCoreRequest {

    @NotNull(message = "symbol is null")
    private String symbol;

    private OrderSide side;

    private PositionSide positionSide = PositionSide.NET;

    private OrderType type;

    private Boolean reduceOnly = false;

    @NotNull(message = "quantity is null")
    @Positive(message = "quantity must be positive")
    private BigDecimal quantity;

    private BigDecimal price;

    private String clientOrderId;

    private BigDecimal stopPrice;

    private Boolean closePosition = false;

    private BigDecimal activationPrice;

    private BigDecimal callbackRate;

    private TimeInForce timeInForce;

    private WorkingType workingType = WorkingType.LAST_PRICE;

    private Boolean priceProtect = false;

    private OTOOrderType otoOrderType = OTOOrderType.NONE;
}
