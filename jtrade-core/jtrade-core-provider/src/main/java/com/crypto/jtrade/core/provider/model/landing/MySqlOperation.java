package com.crypto.jtrade.core.provider.model.landing;

import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.DataAction;
import com.crypto.jtrade.common.constants.DataObject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * mysql operation
 *
 * @author 0xWill
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MySqlOperation {

    private String clientId;

    private DataObject dataObject;

    private DataAction dataAction;

    private String value;

    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        sb.append(clientId).append(Constants.COMMA);
        sb.append(dataObject).append(Constants.COMMA);
        sb.append(dataAction).append(Constants.COMMA);
        sb.append(value);
        return sb.toString();
    }

}
