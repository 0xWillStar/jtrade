package com.crypto.jtrade.core.provider.model.landing;

import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.Position;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * adjust position margin landing
 *
 * @author 0xWill
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdjustPositionMarginLanding {

    private Long requestId;

    private AssetBalance balance;

    private Position position;

}
