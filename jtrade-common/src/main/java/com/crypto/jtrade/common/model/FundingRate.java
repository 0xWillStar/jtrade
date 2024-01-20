package com.crypto.jtrade.common.model;

import java.math.BigDecimal;

import lombok.Data;

/**
 * funding rate
 *
 * @author 0xWill
 */
@Data
public class FundingRate {

    private String symbol;

    private BigDecimal fundingRate;

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

        if (getFundingRate() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"fundingRate\":").append(getFundingRate());
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
