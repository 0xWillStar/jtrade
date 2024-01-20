package com.crypto.jtrade.core.provider.model.landing;

import com.crypto.jtrade.common.model.AssetInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * asset info landing, include all change data related to updating asset information.
 *
 * @author 0xWill
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssetInfoLanding {

    private Long requestId;

    private AssetInfo assetInfo;

}
