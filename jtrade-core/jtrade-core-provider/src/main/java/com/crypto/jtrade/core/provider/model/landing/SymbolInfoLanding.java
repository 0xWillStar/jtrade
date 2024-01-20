package com.crypto.jtrade.core.provider.model.landing;

import com.crypto.jtrade.common.model.SymbolInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * symbol info landing, include all change data related to updating symbol information.
 *
 * @author 0xWill
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SymbolInfoLanding {

    private Long requestId;

    private SymbolInfo symbolInfo;

}
