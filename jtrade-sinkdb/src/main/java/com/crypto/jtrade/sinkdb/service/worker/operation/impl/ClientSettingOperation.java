package com.crypto.jtrade.sinkdb.service.worker.operation.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.crypto.jtrade.common.constants.DataObject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * ClientSetting operation
 *
 * @author 0xWill
 **/
@Component
@Slf4j
public class ClientSettingOperation extends AbstractTableOperation {

    @Getter
    private DataObject dataObject = DataObject.CLIENT_SETTING;

    @Override
    public String insert(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(512);
        sb.append("INSERT INTO t_client_setting(client_id,symbol,leverage,margin_type)");
        sb.append("VALUES(");
        // client_id
        if (values[0].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[0]);
            sb.append("'");
        }
        sb.append(",");
        // symbol
        if (values[1].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[1]);
            sb.append("'");
        }
        sb.append(",");
        // leverage
        if (values[2].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[2]);
        }
        sb.append(",");
        // margin_type
        if (values[3].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[3]);
            sb.append("'");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String delete(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(512);
        sb.append("DELETE FROM t_client_setting WHERE ");
        sb.append("client_id=");
        if (values[0].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[0]);
            sb.append("'");
        }
        sb.append(" AND ");
        sb.append("symbol=");
        if (values[1].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[1]);
            sb.append("'");
        }
        return sb.toString();
    }

}
