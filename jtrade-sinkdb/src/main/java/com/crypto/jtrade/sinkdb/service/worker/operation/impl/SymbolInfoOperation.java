package com.crypto.jtrade.sinkdb.service.worker.operation.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.crypto.jtrade.common.constants.DataObject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * SymbolInfo operation
 *
 * @author 0xWill
 **/
@Component
@Slf4j
public class SymbolInfoOperation extends AbstractTableOperation {

    @Getter
    private DataObject dataObject = DataObject.SYMBOL_INFO;

    @Override
    public String insert(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(1024);
        sb.append(
            "INSERT INTO t_symbol_info(exchange_id,product_type,symbol,underlying,position_type,strike_price,options_type,volume_multiple,price_asset,clear_asset,base_asset,margin_price_type,trade_price_mode,basis_price,min_order_quantity,max_order_quantity,price_tick,quantity_tick,inverse,create_time,ordering_time,trading_time,expire_time,status,max_leverage,default_leverage,maintenance_margin_rate,independent_match_thread,clear_asset_scale,price_asset_scale,quantity_scale,impact_value)");
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
        // product_type
        if (values[1].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[1]);
            sb.append("'");
        }
        sb.append(",");
        // symbol
        if (values[2].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[2]);
            sb.append("'");
        }
        sb.append(",");
        // underlying
        if (values[3].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[3]);
            sb.append("'");
        }
        sb.append(",");
        // position_type
        if (values[4].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[4]);
            sb.append("'");
        }
        sb.append(",");
        // strike_price
        if (values[5].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[5]);
        }
        sb.append(",");
        // options_type
        if (values[6].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[6]);
            sb.append("'");
        }
        sb.append(",");
        // volume_multiple
        if (values[7].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[7]);
        }
        sb.append(",");
        // price_asset
        if (values[8].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[8]);
            sb.append("'");
        }
        sb.append(",");
        // clear_asset
        if (values[9].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[9]);
            sb.append("'");
        }
        sb.append(",");
        // base_asset
        if (values[10].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[10]);
            sb.append("'");
        }
        sb.append(",");
        // margin_price_type
        if (values[11].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[11]);
            sb.append("'");
        }
        sb.append(",");
        // trade_price_mode
        if (values[12].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[12]);
            sb.append("'");
        }
        sb.append(",");
        // basis_price
        if (values[13].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[13]);
        }
        sb.append(",");
        // min_order_quantity
        if (values[14].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[14]);
        }
        sb.append(",");
        // max_order_quantity
        if (values[15].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[15]);
        }
        sb.append(",");
        // price_tick
        if (values[16].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[16]);
        }
        sb.append(",");
        // quantity_tick
        if (values[17].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[17]);
        }
        sb.append(",");
        // inverse
        if (values[18].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[18]);
            sb.append("'");
        }
        sb.append(",");
        // create_time
        if (values[19].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[19]);
        }
        sb.append(",");
        // ordering_time
        if (values[20].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[20]);
        }
        sb.append(",");
        // trading_time
        if (values[21].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[21]);
        }
        sb.append(",");
        // expire_time
        if (values[22].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[22]);
        }
        sb.append(",");
        // status
        if (values[23].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[23]);
            sb.append("'");
        }
        sb.append(",");
        // max_leverage
        if (values[24].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[24]);
        }
        sb.append(",");
        // default_leverage
        if (values[25].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[25]);
        }
        sb.append(",");
        // maintenance_margin_rate
        if (values[26].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[26]);
        }
        sb.append(",");
        // independent_match_thread
        if (values[27].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[27]);
            sb.append("'");
        }
        sb.append(",");
        // clear_asset_scale
        if (values[28].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[28]);
        }
        sb.append(",");
        // price_asset_scale
        if (values[29].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[29]);
        }
        sb.append(",");
        // quantity_scale
        if (values[30].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[30]);
        }
        sb.append(",");
        // impact_value
        if (values[31].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[31]);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String delete(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(512);
        sb.append("DELETE FROM t_symbol_info WHERE ");
        sb.append("symbol=");
        if (values[2].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[2]);
            sb.append("'");
        }
        return sb.toString();
    }

}
