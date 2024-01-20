package com.crypto.jtrade.sinkdb.service.worker.operation.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.crypto.jtrade.common.constants.DataObject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Position operation
 *
 * @author 0xWill
 **/
@Component
@Slf4j
public class PositionOperation extends AbstractTableOperation {

    @Getter
    private DataObject dataObject = DataObject.POSITION;

    @Override
    public String insert(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(1024);
        sb.append(
            "INSERT INTO t_position(exchange_id,member_id,client_id,symbol,position_side,margin_type,position_amt,long_frozen_amt,short_frozen_amt,open_price,position_margin,long_frozen_margin,short_frozen_margin,leverage,asset,auto_add_margin,isolated_balance,isolated_frozen_fee,update_time,reduce_only_order_count)");
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
        // position_side
        if (values[4].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[4]);
            sb.append("'");
        }
        sb.append(",");
        // margin_type
        if (values[5].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[5]);
            sb.append("'");
        }
        sb.append(",");
        // position_amt
        if (values[6].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[6]);
        }
        sb.append(",");
        // long_frozen_amt
        if (values[7].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[7]);
        }
        sb.append(",");
        // short_frozen_amt
        if (values[8].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[8]);
        }
        sb.append(",");
        // open_price
        if (values[9].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[9]);
        }
        sb.append(",");
        // position_margin
        if (values[10].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[10]);
        }
        sb.append(",");
        // long_frozen_margin
        if (values[11].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[11]);
        }
        sb.append(",");
        // short_frozen_margin
        if (values[12].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[12]);
        }
        sb.append(",");
        // leverage
        if (values[13].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[13]);
        }
        sb.append(",");
        // asset
        if (values[14].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[14]);
            sb.append("'");
        }
        sb.append(",");
        // auto_add_margin
        if (values[15].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[15]);
            sb.append("'");
        }
        sb.append(",");
        // isolated_balance
        if (values[16].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[16]);
        }
        sb.append(",");
        // isolated_frozen_fee
        if (values[17].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[17]);
        }
        sb.append(",");
        // update_time
        if (values[18].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[18]);
        }
        sb.append(",");
        // reduce_only_order_count
        if (values[19].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[19]);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String update(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(1024);
        sb.append("UPDATE t_position SET ");
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
        sb.append("margin_type=");
        if (values[5].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[5]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("position_amt=");
        if (values[6].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[6]);
        }
        sb.append(",");
        sb.append("long_frozen_amt=");
        if (values[7].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[7]);
        }
        sb.append(",");
        sb.append("short_frozen_amt=");
        if (values[8].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[8]);
        }
        sb.append(",");
        sb.append("open_price=");
        if (values[9].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[9]);
        }
        sb.append(",");
        sb.append("position_margin=");
        if (values[10].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[10]);
        }
        sb.append(",");
        sb.append("long_frozen_margin=");
        if (values[11].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[11]);
        }
        sb.append(",");
        sb.append("short_frozen_margin=");
        if (values[12].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[12]);
        }
        sb.append(",");
        sb.append("leverage=");
        if (values[13].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[13]);
        }
        sb.append(",");
        sb.append("asset=");
        if (values[14].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[14]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("auto_add_margin=");
        if (values[15].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[15]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("isolated_balance=");
        if (values[16].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[16]);
        }
        sb.append(",");
        sb.append("isolated_frozen_fee=");
        if (values[17].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[17]);
        }
        sb.append(",");
        sb.append("update_time=");
        if (values[18].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[18]);
        }
        sb.append(",");
        sb.append("reduce_only_order_count=");
        if (values[19].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[19]);
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
        sb.append("position_side=");
        if (values[4].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[4]);
            sb.append("'");
        }
        return sb.toString();
    }

    @Override
    public String delete(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(512);
        sb.append("DELETE FROM t_position WHERE ");
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
        sb.append("position_side=");
        if (values[4].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[4]);
            sb.append("'");
        }
        return sb.toString();
    }

}
