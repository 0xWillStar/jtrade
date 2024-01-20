package com.crypto.jtrade.core.provider.model.landing;

import com.crypto.jtrade.common.constants.DataAction;
import com.crypto.jtrade.common.constants.TradeType;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.Bill;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.Trade;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * match landing, include all change data related to order matched.
 *
 * @author 0xWill
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderMatchedLanding {

    private TradeType tradeType;

    private Order order;

    private DataAction orderAction;

    private Position position;

    private DataAction positionAction;

    private AssetBalance balance;

    private DataAction balanceAction;

    private DataAction orderClientAction;

    private DataAction positionClientAction;

    private Trade trade;

    private Bill profitBill;

    private Bill feeBill;

    /**
     * client which no positions and have debts
     */
    private DataAction debtClientAction;

}
