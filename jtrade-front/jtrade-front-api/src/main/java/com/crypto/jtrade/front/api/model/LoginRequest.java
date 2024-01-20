package com.crypto.jtrade.front.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * login request
 *
 * @author 0xWill
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * referral invite code
     */
    private String inviteRef;

}
