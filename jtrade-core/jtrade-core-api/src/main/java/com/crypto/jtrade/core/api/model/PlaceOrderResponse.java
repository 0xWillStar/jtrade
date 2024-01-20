package com.crypto.jtrade.core.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * place order response
 *
 * @author 0xWill
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResponse {

    private String clientOrderId;

    private Long orderId;
}
