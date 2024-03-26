package com.crypto.jtrade.core.provider.model.landing;

import com.crypto.jtrade.common.constants.DataAction;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * place order landing, include all change data related to place order.
 *
 * @author 0xWill
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderLanding {

    private Long requestId;

    private Order order;

    private DataAction orderAction;

    private Position position;

    private DataAction positionAction;

    private AssetBalance balance;

    private DataAction balanceAction;

    private DataAction orderClientAction;

    private boolean stopTriggered;

}
