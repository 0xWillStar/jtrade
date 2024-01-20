package com.crypto.jtrade.common.model;

import com.crypto.jtrade.common.annotation.MyField;
import com.crypto.jtrade.common.annotation.MyType;

import lombok.Data;

/**
 * system parameter
 *
 * @author 0xWill
 */
@Data
@MyType(table = "t_system_parameter")
public class SystemParameter {

    @MyField(key = true)
    private com.crypto.jtrade.common.constants.SystemParameter parameter;

    private String value;

    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        if (getParameter() != null) {
            sb.append(getParameter());
        }
        sb.append(",");
        if (getValue() != null) {
            sb.append(getValue());
        }
        return sb.toString();
    }

}
