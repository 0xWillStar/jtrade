package com.crypto.jtrade.core.api.model;

import java.math.BigDecimal;

import lombok.Data;

/**
 * withdraw request
 *
 * @author 0xWill
 */
@Data
public class WithdrawRequest extends BaseCoreRequest {

    private String asset;

    private BigDecimal amount;

}
