package com.crypto.jtrade.core.provider.model.landing;

import com.crypto.jtrade.common.constants.DataAction;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.Bill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * withdraw landing, include all change data related to withdraw.
 *
 * @author 0xWill
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawLanding {

    private Long requestId;

    private DataAction balanceAction;

    private AssetBalance balance;

    private Bill bill;

}
