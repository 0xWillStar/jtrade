package com.crypto.jtrade.core.provider.model.landing;

import com.crypto.jtrade.common.model.ClientSetting;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * client setting landing, include all change data related to updating symbol information.
 *
 * @author 0xWill
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientSettingLanding {

    private Long requestId;

    private ClientSetting clientSetting;

}
