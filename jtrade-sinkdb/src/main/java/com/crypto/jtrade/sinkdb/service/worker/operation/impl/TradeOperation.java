package com.crypto.jtrade.sinkdb.service.worker.operation.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.crypto.jtrade.common.constants.DataObject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Trade operation
 *
 * @author 0xWill
 **/
@Component
@Slf4j
public class TradeOperation extends AbstractTableOperation {

    @Getter
    private DataObject dataObject = DataObject.TRADE;

    @Override
    public String insert(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(1024);
        sb.append(
            "INSERT INTO t_trade(exchange_id,member_id,client_id,symbol,trade_id,side,position_side,order_id,client_order_id,price,qty,quote_qty,close_profit,fee,fee_asset,match_role,trade_time,trade_type,liquidation_price)");
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
        // trade_id
        if (values[4].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[4]);
        }
        sb.append(",");
        // side
        if (values[5].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[5]);
            sb.append("'");
        }
        sb.append(",");
        // position_side
        if (values[6].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[6]);
            sb.append("'");
        }
        sb.append(",");
        // order_id
        if (values[7].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[7]);
        }
        sb.append(",");
        // client_order_id
        if (values[8].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[8]);
            sb.append("'");
        }
        sb.append(",");
        // price
        if (values[9].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[9]);
        }
        sb.append(",");
        // qty
        if (values[10].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[10]);
        }
        sb.append(",");
        // quote_qty
        if (values[11].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[11]);
        }
        sb.append(",");
        // close_profit
        if (values[12].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[12]);
        }
        sb.append(",");
        // fee
        if (values[13].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[13]);
        }
        sb.append(",");
        // fee_asset
        if (values[14].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[14]);
            sb.append("'");
        }
        sb.append(",");
        // match_role
        if (values[15].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[15]);
            sb.append("'");
        }
        sb.append(",");
        // trade_time
        if (values[16].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[16]);
        }
        sb.append(",");
        // trade_type
        if (values[17].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[17]);
            sb.append("'");
        }
        sb.append(",");
        // liquidation_price
        if (values[18].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[18]);
        }
        sb.append(")");
        return sb.toString();
    }

}
