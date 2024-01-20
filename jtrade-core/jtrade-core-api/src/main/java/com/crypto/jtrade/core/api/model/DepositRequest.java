package com.crypto.jtrade.core.api.model;

import java.math.BigDecimal;

import lombok.Data;

/**
 * deposit request
 *
 * @author 0xWill
 */
@Data
public class DepositRequest extends BaseCoreRequest {

    private String asset;

    private BigDecimal amount;

    private String hash;
}
