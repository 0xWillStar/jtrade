package com.crypto.jtrade.core.provider.model.landing;

import java.util.List;

import com.crypto.jtrade.common.model.FeeRate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * client fee rate landing
 *
 * @author 0xWill
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientFeeRateLanding {

    private Long requestId;

    private List<FeeRate> feeRateList;

}
