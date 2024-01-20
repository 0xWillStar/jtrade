package com.crypto.jtrade.core.provider.model.landing;

import com.crypto.jtrade.common.constants.DataAction;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * cancel order landing, include all change data related to cancel order.
 *
 * @author 0xWill
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCanceledLanding {

    private Order order;

    private Position position;

    private DataAction positionAction;

    private AssetBalance balance;

    private DataAction orderClientAction;

}
