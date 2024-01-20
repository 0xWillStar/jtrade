package com.crypto.jtrade.sinkdb.service.worker.operation.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.crypto.jtrade.common.constants.DataObject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * AssetBalance operation
 *
 * @author 0xWill
 **/
@Component
@Slf4j
public class AssetBalanceOperation extends AbstractTableOperation {

    @Getter
    private DataObject dataObject = DataObject.ASSET_BALANCE;

    @Override
    public String insert(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(1024);
        sb.append(
            "INSERT INTO t_asset_balance(exchange_id,member_id,client_id,asset,pre_balance,balance,deposit,withdraw,position_margin,close_profit,fee,money_change,frozen_margin,frozen_money,frozen_fee,available,withdrawable,position_amt,update_time,row_key,isolated_balance)");
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
        // asset
        if (values[3].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[3]);
            sb.append("'");
        }
        sb.append(",");
        // pre_balance
        if (values[4].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[4]);
        }
        sb.append(",");
        // balance
        if (values[5].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[5]);
        }
        sb.append(",");
        // deposit
        if (values[6].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[6]);
        }
        sb.append(",");
        // withdraw
        if (values[7].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[7]);
        }
        sb.append(",");
        // position_margin
        if (values[8].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[8]);
        }
        sb.append(",");
        // close_profit
        if (values[9].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[9]);
        }
        sb.append(",");
        // fee
        if (values[10].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[10]);
        }
        sb.append(",");
        // money_change
        if (values[11].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[11]);
        }
        sb.append(",");
        // frozen_margin
        if (values[12].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[12]);
        }
        sb.append(",");
        // frozen_money
        if (values[13].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[13]);
        }
        sb.append(",");
        // frozen_fee
        if (values[14].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[14]);
        }
        sb.append(",");
        // available
        if (values[15].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[15]);
        }
        sb.append(",");
        // withdrawable
        if (values[16].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[16]);
        }
        sb.append(",");
        // position_amt
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
        // row_key
        if (values[19].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[19]);
            sb.append("'");
        }
        sb.append(",");
        // isolated_balance
        if (values[20].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[20]);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String update(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(1024);
        sb.append("UPDATE t_asset_balance SET ");
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
        sb.append("pre_balance=");
        if (values[4].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[4]);
        }
        sb.append(",");
        sb.append("balance=");
        if (values[5].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[5]);
        }
        sb.append(",");
        sb.append("deposit=");
        if (values[6].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[6]);
        }
        sb.append(",");
        sb.append("withdraw=");
        if (values[7].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[7]);
        }
        sb.append(",");
        sb.append("position_margin=");
        if (values[8].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[8]);
        }
        sb.append(",");
        sb.append("close_profit=");
        if (values[9].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[9]);
        }
        sb.append(",");
        sb.append("fee=");
        if (values[10].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[10]);
        }
        sb.append(",");
        sb.append("money_change=");
        if (values[11].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[11]);
        }
        sb.append(",");
        sb.append("frozen_margin=");
        if (values[12].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[12]);
        }
        sb.append(",");
        sb.append("frozen_money=");
        if (values[13].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[13]);
        }
        sb.append(",");
        sb.append("frozen_fee=");
        if (values[14].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[14]);
        }
        sb.append(",");
        sb.append("available=");
        if (values[15].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[15]);
        }
        sb.append(",");
        sb.append("withdrawable=");
        if (values[16].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[16]);
        }
        sb.append(",");
        sb.append("position_amt=");
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
        sb.append("row_key=");
        if (values[19].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[19]);
            sb.append("'");
        }
        sb.append(",");
        sb.append("isolated_balance=");
        if (values[20].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[20]);
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
        sb.append("asset=");
        if (values[3].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[3]);
            sb.append("'");
        }
        return sb.toString();
    }

    @Override
    public String delete(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(512);
        sb.append("DELETE FROM t_asset_balance WHERE ");
        sb.append("client_id=");
        if (values[2].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[2]);
            sb.append("'");
        }
        sb.append(" AND ");
        sb.append("asset=");
        if (values[3].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[3]);
            sb.append("'");
        }
        return sb.toString();
    }

}
