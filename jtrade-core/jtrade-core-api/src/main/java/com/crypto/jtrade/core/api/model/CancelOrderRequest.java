package com.crypto.jtrade.core.api.model;

import lombok.Data;

/**
 * cancel order request
 *
 * @author 0xWill
 */
@Data
public class CancelOrderRequest extends BaseCoreRequest {

    private String symbol;

    private Long orderId;

    private String clientOrderId;
}
