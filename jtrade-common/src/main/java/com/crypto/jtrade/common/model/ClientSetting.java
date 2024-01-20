package com.crypto.jtrade.common.model;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.crypto.jtrade.common.annotation.MyField;
import com.crypto.jtrade.common.annotation.MyType;
import com.crypto.jtrade.common.constants.MarginType;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * client settings such as leverage、margin type
 *
 * @author 0xWill
 **/
@Data
@MyType(table = "t_client_setting")
public class ClientSetting {

    @MyField(key = true)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String clientId;

    @MyField(key = true)
    private String symbol;

    private BigDecimal leverage;

    private MarginType marginType;

    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        if (getClientId() != null) {
            sb.append(getClientId());
        }
        sb.append(",");
        if (getSymbol() != null) {
            sb.append(getSymbol());
        }
        sb.append(",");
        if (getLeverage() != null) {
            sb.append(getLeverage());
        }
        sb.append(",");
        if (getMarginType() != null) {
            sb.append(getMarginType());
        }
        return sb.toString();
    }

    public static ClientSetting toObject(String str) {
        ClientSetting obj = new ClientSetting();
        String[] values = StringUtils.splitPreserveAllTokens(str, ',');
        if (!values[0].equals("")) {
            obj.setClientId(values[0]);
        }

        if (!values[1].equals("")) {
            obj.setSymbol(values[1]);
        }

        if (!values[2].equals("")) {
            obj.setLeverage(new BigDecimal(values[2]));
        }

        if (!values[3].equals("")) {
            obj.setMarginType(MarginType.valueOf(values[3]));
        }

        return obj;
    }

}
