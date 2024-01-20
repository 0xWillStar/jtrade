package com.crypto.jtrade.core.provider.model.landing;

import java.util.Collection;

import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.Bill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * funding fee landing
 *
 * @author 0xWill
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundingFeeLanding {

    private Long requestId;

    private Collection<AssetBalance> balances;

    private Collection<Bill> bills;

}
