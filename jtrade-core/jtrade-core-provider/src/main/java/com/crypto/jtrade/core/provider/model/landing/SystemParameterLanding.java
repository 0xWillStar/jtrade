package com.crypto.jtrade.core.provider.model.landing;

import com.crypto.jtrade.common.model.SystemParameter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * system parameter landing, include all change data related to updating system parameter.
 *
 * @author 0xWill
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemParameterLanding {

    private Long requestId;

    private SystemParameter parameter;

}
