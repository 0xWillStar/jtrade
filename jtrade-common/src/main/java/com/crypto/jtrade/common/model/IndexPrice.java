package com.crypto.jtrade.common.model;

import java.math.BigDecimal;

import lombok.Data;

/**
 * mark price
 *
 * @author 0xWill
 */
@Data
public class IndexPrice {

    private String symbol;

    private BigDecimal indexPrice;

    private Long time;

    public String toJSONString() {
        StringBuilder sb = new StringBuilder(256);
        boolean first = true;
        sb.append("{");
        if (getSymbol() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"symbol\":\"").append(getSymbol()).append("\"");
        }

        if (getIndexPrice() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"indexPrice\":").append(getIndexPrice());
        }

        if (getTime() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"time\":").append(getTime());
        }

        sb.append("}");
        return sb.toString();
    }

}
