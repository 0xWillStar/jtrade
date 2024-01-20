package com.crypto.jtrade.core.provider.model.landing;

import java.util.List;
import java.util.Map;

import com.crypto.jtrade.common.constants.DataAction;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.Bill;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * landing when deduct collateral asset
 *
 * @author 0xWill
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeductCollateralLanding {

    private Long requestId;

    private String sellClientId;

    private List<AssetBalance> sellBalanceList;

    private List<Bill> sellBillList;

    private String buyClientId;

    private Map<AssetBalance, DataAction> buyBalanceMap;

    private List<Bill> buyBillList;

    /**
     * client which no positions and have debts
     */
    private DataAction debtClientAction;

}
