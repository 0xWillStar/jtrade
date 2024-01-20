package com.crypto.jtrade.common.model;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.crypto.jtrade.common.annotation.MyField;
import com.crypto.jtrade.common.annotation.MyType;
import com.crypto.jtrade.common.constants.MarginType;
import com.crypto.jtrade.common.constants.OrderSide;
import com.crypto.jtrade.common.constants.PositionSide;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * Position information
 *
 * @author 0xWill
 **/
@Data
@MyType(table = "t_position")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Position {

    @MyField(json = false)
    @JsonIgnore
    private String exchangeId;

    @MyField(json = false)
    @JsonIgnore
    private String memberId;

    @MyField(key = true)
    @JsonIgnore
    private String clientId;

    @MyField(key = true)
    private String symbol;

    @MyField(key = true)
    private PositionSide positionSide;

    private MarginType marginType;

    /**
     * If positionSide is NET, a positive positionAmt indicates a long position; a negative positionAmt indicates a
     * short position.
     */
    private BigDecimal positionAmt;

    private BigDecimal longFrozenAmt;

    private BigDecimal shortFrozenAmt;

    private BigDecimal openPrice;

    /**
     * If dynamic margin, positionMargin = 0
     */
    private BigDecimal positionMargin;

    private BigDecimal longFrozenMargin;

    private BigDecimal shortFrozenMargin;

    /**
     * redundancy field
     */
    private BigDecimal leverage;

    /**
     * redundancy field
     */
    private String asset;

    /**
     * If margin type is ISOLATED, whether to automatically increase the margin
     */
    private Boolean autoAddMargin;

    /**
     * used when margin type is ISOLATED
     */
    private BigDecimal isolatedBalance;

    /**
     * used when margin type is ISOLATED
     */
    private BigDecimal isolatedFrozenFee;

    private Long updateTime;

    /**
     * for easy to check if there is ReduceOnly order.
     */
    @MyField(json = false)
    @JsonIgnore
    private Integer reduceOnlyOrderCount;

    /**
     * markPrice、maintenanceMargin、unrealizedProfit are for easy to calculate liquidation price.
     */
    @MyField(text = false, json = false)
    @JsonIgnore
    private BigDecimal markPrice;

    @MyField(text = false, json = false)
    @JsonIgnore
    private BigDecimal maintenanceMargin;

    @MyField(text = false, json = false)
    @JsonIgnore
    private BigDecimal unrealizedProfit;

    /**
     * Create a position instance when placing order for the first time
     */
    public static Position createPosition(Order order, MarginType marginType, String asset) {
        Position position = new Position();
        position.setClientId(order.getClientId());
        position.setSymbol(order.getSymbol());
        position.setPositionSide(order.getPositionSide());
        position.setMarginType(marginType);
        position.setPositionAmt(BigDecimal.ZERO);
        position.setLongFrozenAmt(order.getSide() == OrderSide.BUY ? order.getQuantity() : BigDecimal.ZERO);
        position.setShortFrozenAmt(order.getSide() == OrderSide.SELL ? order.getQuantity() : BigDecimal.ZERO);
        position.setOpenPrice(BigDecimal.ZERO);
        position.setPositionMargin(BigDecimal.ZERO);
        position.setLongFrozenMargin(order.getSide() == OrderSide.BUY ? order.getFrozenMargin() : BigDecimal.ZERO);
        position.setShortFrozenMargin(order.getSide() == OrderSide.SELL ? order.getFrozenMargin() : BigDecimal.ZERO);
        position.setLeverage(order.getLeverage());
        position.setAsset(asset);
        position.setAutoAddMargin(false);
        position.setIsolatedBalance(BigDecimal.ZERO);
        position.setIsolatedFrozenFee(BigDecimal.ZERO);
        position.setUpdateTime(System.currentTimeMillis());
        position.setReduceOnlyOrderCount(0);
        return position;
    }

    /**
     * Create a position instance
     */
    public static Position createPosition(String clientId, String symbol, MarginType marginType, String asset,
        PositionSide positionSide) {
        Position position = new Position();
        position.setClientId(clientId);
        position.setSymbol(symbol);
        position.setPositionSide(positionSide);
        position.setMarginType(marginType);
        position.setPositionAmt(BigDecimal.ZERO);
        position.setLongFrozenAmt(BigDecimal.ZERO);
        position.setShortFrozenAmt(BigDecimal.ZERO);
        position.setOpenPrice(BigDecimal.ZERO);
        position.setPositionMargin(BigDecimal.ZERO);
        position.setLongFrozenMargin(BigDecimal.ZERO);
        position.setShortFrozenMargin(BigDecimal.ZERO);
        position.setLeverage(BigDecimal.ZERO);
        position.setAsset(asset);
        position.setAutoAddMargin(false);
        position.setIsolatedBalance(BigDecimal.ZERO);
        position.setIsolatedFrozenFee(BigDecimal.ZERO);
        position.setUpdateTime(System.currentTimeMillis());
        position.setReduceOnlyOrderCount(0);
        return position;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        if (getExchangeId() != null) {
            sb.append(getExchangeId());
        }
        sb.append(",");
        if (getMemberId() != null) {
            sb.append(getMemberId());
        }
        sb.append(",");
        if (getClientId() != null) {
            sb.append(getClientId());
        }
        sb.append(",");
        if (getSymbol() != null) {
            sb.append(getSymbol());
        }
        sb.append(",");
        if (getPositionSide() != null) {
            sb.append(getPositionSide());
        }
        sb.append(",");
        if (getMarginType() != null) {
            sb.append(getMarginType());
        }
        sb.append(",");
        if (getPositionAmt() != null) {
            sb.append(getPositionAmt());
        }
        sb.append(",");
        if (getLongFrozenAmt() != null) {
            sb.append(getLongFrozenAmt());
        }
        sb.append(",");
        if (getShortFrozenAmt() != null) {
            sb.append(getShortFrozenAmt());
        }
        sb.append(",");
        if (getOpenPrice() != null) {
            sb.append(getOpenPrice());
        }
        sb.append(",");
        if (getPositionMargin() != null) {
            sb.append(getPositionMargin());
        }
        sb.append(",");
        if (getLongFrozenMargin() != null) {
            sb.append(getLongFrozenMargin());
        }
        sb.append(",");
        if (getShortFrozenMargin() != null) {
            sb.append(getShortFrozenMargin());
        }
        sb.append(",");
        if (getLeverage() != null) {
            sb.append(getLeverage());
        }
        sb.append(",");
        if (getAsset() != null) {
            sb.append(getAsset());
        }
        sb.append(",");
        if (getAutoAddMargin() != null) {
            sb.append(getAutoAddMargin());
        }
        sb.append(",");
        if (getIsolatedBalance() != null) {
            sb.append(getIsolatedBalance());
        }
        sb.append(",");
        if (getIsolatedFrozenFee() != null) {
            sb.append(getIsolatedFrozenFee());
        }
        sb.append(",");
        if (getUpdateTime() != null) {
            sb.append(getUpdateTime());
        }
        sb.append(",");
        if (getReduceOnlyOrderCount() != null) {
            sb.append(getReduceOnlyOrderCount());
        }
        return sb.toString();
    }

    public static Position toObject(String str) {
        Position obj = new Position();
        String[] values = StringUtils.splitPreserveAllTokens(str, ',');
        if (!values[0].equals("")) {
            obj.setExchangeId(values[0]);
        }

        if (!values[1].equals("")) {
            obj.setMemberId(values[1]);
        }

        if (!values[2].equals("")) {
            obj.setClientId(values[2]);
        }

        if (!values[3].equals("")) {
            obj.setSymbol(values[3]);
        }

        if (!values[4].equals("")) {
            obj.setPositionSide(PositionSide.valueOf(values[4]));
        }

        if (!values[5].equals("")) {
            obj.setMarginType(MarginType.valueOf(values[5]));
        }

        if (!values[6].equals("")) {
            obj.setPositionAmt(new BigDecimal(values[6]));
        }

        if (!values[7].equals("")) {
            obj.setLongFrozenAmt(new BigDecimal(values[7]));
        }

        if (!values[8].equals("")) {
            obj.setShortFrozenAmt(new BigDecimal(values[8]));
        }

        if (!values[9].equals("")) {
            obj.setOpenPrice(new BigDecimal(values[9]));
        }

        if (!values[10].equals("")) {
            obj.setPositionMargin(new BigDecimal(values[10]));
        }

        if (!values[11].equals("")) {
            obj.setLongFrozenMargin(new BigDecimal(values[11]));
        }

        if (!values[12].equals("")) {
            obj.setShortFrozenMargin(new BigDecimal(values[12]));
        }

        if (!values[13].equals("")) {
            obj.setLeverage(new BigDecimal(values[13]));
        }

        if (!values[14].equals("")) {
            obj.setAsset(values[14]);
        }

        if (!values[15].equals("")) {
            obj.setAutoAddMargin(Boolean.valueOf(values[15]));
        }

        if (!values[16].equals("")) {
            obj.setIsolatedBalance(new BigDecimal(values[16]));
        }

        if (!values[17].equals("")) {
            obj.setIsolatedFrozenFee(new BigDecimal(values[17]));
        }

        if (!values[18].equals("")) {
            obj.setUpdateTime(Long.parseLong(values[18]));
        }

        if (!values[19].equals("")) {
            obj.setReduceOnlyOrderCount(Integer.parseInt(values[19]));
        }

        return obj;
    }

    public String toJSONString() {
        StringBuilder sb = new StringBuilder(1024);
        boolean first = true;
        sb.append("{");
        if (getClientId() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"clientId\":\"").append(getClientId()).append("\"");
        }

        if (getSymbol() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"symbol\":\"").append(getSymbol()).append("\"");
        }

        if (getPositionSide() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"positionSide\":\"").append(getPositionSide()).append("\"");
        }

        if (getMarginType() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"marginType\":\"").append(getMarginType()).append("\"");
        }

        if (getPositionAmt() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"positionAmt\":").append(getPositionAmt());
        }

        if (getLongFrozenAmt() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"longFrozenAmt\":").append(getLongFrozenAmt());
        }

        if (getShortFrozenAmt() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"shortFrozenAmt\":").append(getShortFrozenAmt());
        }

        if (getOpenPrice() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"openPrice\":").append(getOpenPrice());
        }

        if (getPositionMargin() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"positionMargin\":").append(getPositionMargin());
        }

        if (getLongFrozenMargin() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"longFrozenMargin\":").append(getLongFrozenMargin());
        }

        if (getShortFrozenMargin() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"shortFrozenMargin\":").append(getShortFrozenMargin());
        }

        if (getLeverage() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"leverage\":").append(getLeverage());
        }

        if (getAsset() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"asset\":\"").append(getAsset()).append("\"");
        }

        if (getAutoAddMargin() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"autoAddMargin\":").append(getAutoAddMargin());
        }

        if (getIsolatedBalance() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"isolatedBalance\":").append(getIsolatedBalance());
        }

        if (getIsolatedFrozenFee() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"isolatedFrozenFee\":").append(getIsolatedFrozenFee());
        }

        if (getUpdateTime() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"updateTime\":").append(getUpdateTime());
        }

        sb.append("}");
        return sb.toString();
    }

}
