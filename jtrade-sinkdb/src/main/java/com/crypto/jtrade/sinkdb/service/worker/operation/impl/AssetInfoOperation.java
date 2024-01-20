package com.crypto.jtrade.sinkdb.service.worker.operation.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.crypto.jtrade.common.constants.DataObject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * AssetInfo operation
 *
 * @author 0xWill
 **/
@Component
@Slf4j
public class AssetInfoOperation extends AbstractTableOperation {

    @Getter
    private DataObject dataObject = DataObject.ASSET_INFO;

    @Override
    public String insert(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(512);
        sb.append("INSERT INTO t_asset_info(asset,index_price_symbol,discount,deduct_order,scale)");
        sb.append("VALUES(");
        // asset
        if (values[0].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[0]);
            sb.append("'");
        }
        sb.append(",");
        // index_price_symbol
        if (values[1].equals("")) {
            sb.append("null");
        } else {
            sb.append("'");
            sb.append(values[1]);
            sb.append("'");
        }
        sb.append(",");
        // discount
        if (values[2].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[2]);
        }
        sb.append(",");
        // deduct_order
        if (values[3].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[3]);
        }
        sb.append(",");
        // scale
        if (values[4].equals("")) {
            sb.append("null");
        } else {
            sb.append(values[4]);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String delete(String data) {
        String[] values = StringUtils.splitPreserveAllTokens(data, ',');
        StringBuilder sb = new StringBuilder(512);
        sb.append("DELETE FROM t_asset_info WHERE ");
        sb.append("asset=");
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
