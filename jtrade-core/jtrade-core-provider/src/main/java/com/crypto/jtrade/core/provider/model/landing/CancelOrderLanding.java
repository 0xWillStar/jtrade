package com.crypto.jtrade.core.provider.model.landing;

import com.crypto.jtrade.common.constants.DataAction;
import com.crypto.jtrade.common.model.Order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * cancel order landing for stop order.
 *
 * @author 0xWill
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelOrderLanding {

    private Long requestId;

    private Order order;

    private DataAction orderClientAction;

}
