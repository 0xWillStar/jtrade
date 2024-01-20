package com.crypto.jtrade.core.api.model;

import lombok.Data;

/**
 * base core request
 *
 * @author 0xWill
 */
@Data
public class BaseCoreRequest {

    private String memberId;

    private String clientId;

    private String userId;

    private String exchangeId;
}
