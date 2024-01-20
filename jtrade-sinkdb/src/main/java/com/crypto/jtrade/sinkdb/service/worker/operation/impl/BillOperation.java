package com.crypto.jtrade.sinkdb.service.worker.operation.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.crypto.jtrade.common.constants.DataObject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Bill operation
 *
 * @author 0xWill
 **/
@Component
@Slf4j
public class BillOperation extends AbstractTableOperation {

    @Getter
    private DataObject dataObject = DataObject.BILL;

    @Override
    public String insert(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(1024);
        sb.append(
            "INSERT INTO t_bill(exchange_id,member_id,client_id,symbol,bill_type,asset,amount,info,correlation_id,insert_time)");
        sb.append("VALUES(");
        // exchange_id
        if (values[0].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[0]);
            sb.append("'");
        }
        sb.append(",");
        // member_id
        if (values[1].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[1]);
            sb.append("'");
        }
        sb.append(",");
        // client_id
        if (values[2].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[2]);
            sb.append("'");
        }
        sb.append(",");
        // symbol
        if (values[3].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[3]);
            sb.append("'");
        }
        sb.append(",");
        // bill_type
        if (values[4].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[4]);
            sb.append("'");
        }
        sb.append(",");
        // asset
        if (values[5].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[5]);
            sb.append("'");
        }
        sb.append(",");
        // amount
        if (values[6].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[6]);
        }
        sb.append(",");
        // info
        if (values[7].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[7]);
            sb.append("'");
        }
        sb.append(",");
        // correlation_id
        if (values[8].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[8]);
            sb.append("'");
        }
        sb.append(",");
        // insert_time
        if (values[9].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[9]);
        }
        sb.append(")");
        return sb.toString();
    }

}
