package com.crypto.jtrade.core.provider.model.landing;

import com.crypto.jtrade.common.constants.DataAction;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.Bill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * deposit landing, include all change data related to deposit.
 *
 * @author 0xWill
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DepositLanding {

    private Long requestId;

    private DataAction balanceAction;

    private AssetBalance balance;

    private Bill bill;

    /**
     * client which no positions and have debts
     */
    private DataAction debtClientAction;

}
