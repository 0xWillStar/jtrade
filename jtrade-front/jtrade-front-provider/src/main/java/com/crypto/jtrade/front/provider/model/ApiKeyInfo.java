package com.crypto.jtrade.front.provider.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * api key information
 *
 * @author 0xWill
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiKeyInfo {

    private String apiKey;

    private String apiSecret;

    private String clientId;

}
