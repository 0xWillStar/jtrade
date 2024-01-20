package com.crypto.jtrade.sinkdb.service.worker.operation.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.crypto.jtrade.common.constants.DataObject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Order operation
 *
 * @author 0xWill
 **/
@Component
@Slf4j
public class OrderOperation extends AbstractTableOperation {

    @Getter
    private DataObject dataObject = DataObject.ORDER;

    @Override
    public String insert(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(1024);
        sb.append(
            "INSERT INTO t_order(exchange_id,member_id,client_id,symbol,side,position_side,status,price,quantity,orig_type,type,time_in_force,order_id,client_order_id,reduce_only,working_type,stop_price,close_position,activation_price,callback_rate,price_protect,order_time,update_time,frozen_fee,frozen_margin,cum_quote,executed_qty,avg_price,fee,fee_asset,close_profit,leverage,left_qty,margin_type,first_isolated_order,oto_order_type,sub_order_id1,sub_order_id2)");
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
        // side
        if (values[4].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[4]);
            sb.append("'");
        }
        sb.append(",");
        // position_side
        if (values[5].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[5]);
            sb.append("'");
        }
        sb.append(",");
        // status
        if (values[6].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[6]);
            sb.append("'");
        }
        sb.append(",");
        // price
        if (values[7].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[7]);
        }
        sb.append(",");
        // quantity
        if (values[8].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[8]);
        }
        sb.append(",");
        // orig_type
        if (values[9].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[9]);
            sb.append("'");
        }
        sb.append(",");
        // type
        if (values[10].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[10]);
            sb.append("'");
        }
        sb.append(",");
        // time_in_force
        if (values[11].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[11]);
            sb.append("'");
        }
        sb.append(",");
        // order_id
        if (values[12].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[12]);
        }
        sb.append(",");
        // client_order_id
        if (values[13].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[13]);
            sb.append("'");
        }
        sb.append(",");
        // reduce_only
        if (values[14].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[14]);
            sb.append("'");
        }
        sb.append(",");
        // working_type
        if (values[15].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[15]);
            sb.append("'");
        }
        sb.append(",");
        // stop_price
        if (values[16].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[16]);
        }
        sb.append(",");
        // close_position
        if (values[17].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[17]);
            sb.append("'");
        }
        sb.append(",");
        // activation_price
        if (values[18].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[18]);
        }
        sb.append(",");
        // callback_rate
        if (values[19].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[19]);
        }
        sb.append(",");
        // price_protect
        if (values[20].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[20]);
            sb.append("'");
        }
        sb.append(",");
        // order_time
        if (values[21].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[21]);
        }
        sb.append(",");
        // update_time
        if (values[22].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[22]);
        }
        sb.append(",");
        // frozen_fee
        if (values[23].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[23]);
        }
        sb.append(",");
        // frozen_margin
        if (values[24].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[24]);
        }
        sb.append(",");
        // cum_quote
        if (values[25].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[25]);
        }
        sb.append(",");
        // executed_qty
        if (values[26].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[26]);
        }
        sb.append(",");
        // avg_price
        if (values[27].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[27]);
        }
        sb.append(",");
        // fee
        if (values[28].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[28]);
        }
        sb.append(",");
        // fee_asset
        if (values[29].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[29]);
            sb.append("'");
        }
        sb.append(",");
        // close_profit
        if (values[30].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[30]);
        }
        sb.append(",");
        // leverage
        if (values[31].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[31]);
        }
        sb.append(",");
        // left_qty
        if (values[32].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[32]);
        }
        sb.append(",");
        // margin_type
        if (values[33].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[33]);
            sb.append("'");
        }
        sb.append(",");
        // first_isolated_order
        if (values[34].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[34]);
            sb.append("'");
        }
        sb.append(",");
        // oto_order_type
        if (values[35].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[35]);
            sb.append("'");
        }
        sb.append(",");
        // sub_order_id1
        if (values[36].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[36]);
        }
        sb.append(",");
        // sub_order_id2
        if (values[37].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[37]);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String update(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(1024);
        sb.append("UPDATE t_order SET ");
        sb.append("exchange_id=");
        if (values[0].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[0]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("member_id=");
        if (values[1].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[1]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("side=");
        if (values[4].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[4]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("position_side=");
        if (values[5].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[5]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("status=");
        if (values[6].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[6]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("price=");
        if (values[7].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[7]);
        }
        sb.append(",");
        sb.append("quantity=");
        if (values[8].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[8]);
        }
        sb.append(",");
        sb.append("orig_type=");
        if (values[9].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[9]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("type=");
        if (values[10].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[10]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("time_in_force=");
        if (values[11].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[11]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("client_order_id=");
        if (values[13].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[13]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("reduce_only=");
        if (values[14].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[14]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("working_type=");
        if (values[15].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[15]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("stop_price=");
        if (values[16].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[16]);
        }
        sb.append(",");
        sb.append("close_position=");
        if (values[17].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[17]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("activation_price=");
        if (values[18].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[18]);
        }
        sb.append(",");
        sb.append("callback_rate=");
        if (values[19].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[19]);
        }
        sb.append(",");
        sb.append("price_protect=");
        if (values[20].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[20]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("order_time=");
        if (values[21].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[21]);
        }
        sb.append(",");
        sb.append("update_time=");
        if (values[22].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[22]);
        }
        sb.append(",");
        sb.append("frozen_fee=");
        if (values[23].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[23]);
        }
        sb.append(",");
        sb.append("frozen_margin=");
        if (values[24].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[24]);
        }
        sb.append(",");
        sb.append("cum_quote=");
        if (values[25].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[25]);
        }
        sb.append(",");
        sb.append("executed_qty=");
        if (values[26].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[26]);
        }
        sb.append(",");
        sb.append("avg_price=");
        if (values[27].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[27]);
        }
        sb.append(",");
        sb.append("fee=");
        if (values[28].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[28]);
        }
        sb.append(",");
        sb.append("fee_asset=");
        if (values[29].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[29]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("close_profit=");
        if (values[30].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[30]);
        }
        sb.append(",");
        sb.append("leverage=");
        if (values[31].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[31]);
        }
        sb.append(",");
        sb.append("left_qty=");
        if (values[32].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[32]);
        }
        sb.append(",");
        sb.append("margin_type=");
        if (values[33].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[33]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("first_isolated_order=");
        if (values[34].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[34]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("oto_order_type=");
        if (values[35].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[35]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("sub_order_id1=");
        if (values[36].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[36]);
        }
        sb.append(",");
        sb.append("sub_order_id2=");
        if (values[37].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[37]);
        }
        sb.append(" WHERE ");
        sb.append("client_id=");
        if (values[2].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[2]);
            sb.append("'");
        }
        sb.append(" AND ");
        sb.append("symbol=");
        if (values[3].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[3]);
            sb.append("'");
        }
        sb.append(" AND ");
        sb.append("order_id=");
        if (values[12].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[12]);
        }
        return sb.toString();
    }

    @Override
    public String delete(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(512);
        sb.append("DELETE FROM t_order WHERE ");
        sb.append("client_id=");
        if (values[2].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[2]);
            sb.append("'");
        }
        sb.append(" AND ");
        sb.append("symbol=");
        if (values[3].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[3]);
            sb.append("'");
        }
        sb.append(" AND ");
        sb.append("order_id=");
        if (values[12].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[12]);
        }
        return sb.toString();
    }

}
