package com.crypto.jtrade.common.model;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.crypto.jtrade.common.annotation.MyField;
import com.crypto.jtrade.common.annotation.MyType;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * fee rate
 *
 * @author 0xWill
 **/
@Data
@MyType(table = "t_client_fee_rate")
public class FeeRate {

    @MyField(key = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    String clientId;

    @JsonProperty("makerFeeRate")
    @JsonAlias("maker")
    BigDecimal maker;

    @JsonProperty("takerFeeRate")
    @JsonAlias("taker")
    BigDecimal taker;

    public FeeRate() {

    }

    public FeeRate(String clientId, BigDecimal maker, BigDecimal taker) {
        this.setClientId(clientId);
        this.setMaker(maker);
        this.setTaker(taker);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        if (getClientId() != null) {
            sb.append(getClientId());
        }
        sb.append(",");
        if (getMaker() != null) {
            sb.append(getMaker());
        }
        sb.append(",");
        if (getTaker() != null) {
            sb.append(getTaker());
        }
        return sb.toString();
    }

    public static FeeRate toObject(String str) {
        FeeRate obj = new FeeRate();
        String[] values = StringUtils.splitPreserveAllTokens(str, ',');
        if (!values[0].equals("")) {
            obj.setClientId(values[0]);
        }

        if (!values[1].equals("")) {
            obj.setMaker(new BigDecimal(values[1]));
        }

        if (!values[2].equals("")) {
            obj.setTaker(new BigDecimal(values[2]));
        }

        return obj;
    }

}
