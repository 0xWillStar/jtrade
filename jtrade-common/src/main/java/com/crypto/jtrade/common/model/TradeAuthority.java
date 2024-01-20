package com.crypto.jtrade.common.model;

import com.crypto.jtrade.common.annotation.MyField;
import com.crypto.jtrade.common.annotation.MyType;

import lombok.Data;

/**
 * trade authority
 *
 * @author 0xWill
 */
@Data
@MyType(table = "t_client_authority")
public class TradeAuthority {

    @MyField(key = true)
    private String clientId;

    /**
     * Bitwise representation, from low to high: the first bit means whether a position can be opened the second bit
     * means whether a position can be closed the third bit means whether an order can be canceled
     */
    private Integer tradeAuthority;

    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        if (getClientId() != null) {
            sb.append(getClientId());
        }
        sb.append(",");
        if (getTradeAuthority() != null) {
            sb.append(getTradeAuthority());
        }
        return sb.toString();
    }
}
