package com.crypto.jtrade.sinkdb.service.worker.operation.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.crypto.jtrade.common.constants.DataObject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * TradeAuthority operation
 *
 * @author 0xWill
 **/
@Component
@Slf4j
public class ClientAuthorityOperation extends AbstractTableOperation {

    @Getter
    private DataObject dataObject = DataObject.CLIENT_AUTHORITY;

    @Override
    public String insert(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(512);
        sb.append("INSERT INTO t_client_authority(client_id,trade_authority)");
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
        // trade_authority
        if (values[1].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[1]);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String delete(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(512);
        sb.append("DELETE FROM t_client_authority WHERE ");
        sb.append("client_id=");
        if (values[0].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[0]);
            sb.append("'");
        }
        return sb.toString();
    }

}
