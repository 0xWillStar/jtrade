package com.crypto.jtrade.common.model;

import java.math.BigDecimal;

import com.crypto.jtrade.common.annotation.MyField;
import com.crypto.jtrade.common.annotation.MyType;
import org.apache.commons.lang3.StringUtils;

import lombok.Data;

/**
 * Asset information
 *
 * @author 0xWill
 **/
@Data
@MyType(table = "t_asset_info")
public class AssetInfo {

    @MyField(key = true)
    private String asset;

    /**
     * the index price of the corresponding symbol
     */
    private String indexPriceSymbol;

    private BigDecimal discount;

    private Integer deductOrder;

    private Integer scale;

    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        if (getAsset() != null) {
            sb.append(getAsset());
        }
        sb.append(",");
        if (getIndexPriceSymbol() != null) {
            sb.append(getIndexPriceSymbol());
        }
        sb.append(",");
        if (getDiscount() != null) {
            sb.append(getDiscount());
        }
        sb.append(",");
        if (getDeductOrder() != null) {
            sb.append(getDeductOrder());
        }
        sb.append(",");
        if (getScale() != null) {
            sb.append(getScale());
        }
        return sb.toString();
    }

    public static AssetInfo toObject(String str) {
        AssetInfo obj = new AssetInfo();
        String[] values = StringUtils.splitPreserveAllTokens(str, ',');
        if (!values[0].equals("")) {
            obj.setAsset(values[0]);
        }

        if (!values[1].equals("")) {
            obj.setIndexPriceSymbol(values[1]);
        }

        if (!values[2].equals("")) {
            obj.setDiscount(new BigDecimal(values[2]));
        }

        if (!values[3].equals("")) {
            obj.setDeductOrder(Integer.parseInt(values[3]));
        }

        if (!values[4].equals("")) {
            obj.setScale(Integer.parseInt(values[4]));
        }

        return obj;
    }

}
