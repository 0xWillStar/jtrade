package com.crypto.jtrade.front.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * login response
 *
 * @author 0xWill
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String apiKey;

    private String apiSecret;

}
