package com.crypto.jtrade.common.model;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;

import com.crypto.jtrade.common.annotation.MyField;
import com.crypto.jtrade.common.annotation.MyType;
import com.crypto.jtrade.common.constants.MarginType;
import com.crypto.jtrade.common.constants.OTOOrderType;
import com.crypto.jtrade.common.constants.OrderSide;
import com.crypto.jtrade.common.constants.OrderStatus;
import com.crypto.jtrade.common.constants.OrderType;
import com.crypto.jtrade.common.constants.PositionSide;
import com.crypto.jtrade.common.constants.TimeInForce;
import com.crypto.jtrade.common.constants.WorkingType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * Order information
 *
 * @author 0xWill
 **/
@Data
@MyType(table = "t_order")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Order {

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

    private OrderSide side;

    private PositionSide positionSide;

    private OrderStatus status;

    private BigDecimal price;

    private BigDecimal quantity;

    private OrderType origType;

    private OrderType type;

    private TimeInForce timeInForce;

    @MyField(key = true)
    private Long orderId;

    private String clientOrderId;

    private Boolean reduceOnly;

    private WorkingType workingType;

    private BigDecimal stopPrice;

    private Boolean closePosition;

    private BigDecimal activationPrice;

    private BigDecimal callbackRate;

    private Boolean priceProtect;

    private Long orderTime;

    private Long updateTime;

    private BigDecimal frozenFee = BigDecimal.ZERO;

    private BigDecimal frozenMargin = BigDecimal.ZERO;

    private BigDecimal cumQuote = BigDecimal.ZERO;

    private BigDecimal executedQty = BigDecimal.ZERO;

    private BigDecimal avgPrice = BigDecimal.ZERO;

    private BigDecimal fee = BigDecimal.ZERO;

    private String feeAsset;

    private BigDecimal closeProfit = BigDecimal.ZERO;

    private BigDecimal leverage;

    private BigDecimal leftQty = BigDecimal.ZERO;

    @MyField(json = false)
    @JsonIgnore
    private MarginType marginType;

    @MyField(json = false)
    @JsonIgnore
    private Boolean firstIsolatedOrder;

    /**
     * used for OTO order
     */
    private OTOOrderType otoOrderType = OTOOrderType.NONE;
    /**
     * used for OTO order
     */
    private Long subOrderId1;
    /**
     * used for OTO order
     */
    private Long subOrderId2;

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
        if (getSide() != null) {
            sb.append(getSide());
        }
        sb.append(",");
        if (getPositionSide() != null) {
            sb.append(getPositionSide());
        }
        sb.append(",");
        if (getStatus() != null) {
            sb.append(getStatus());
        }
        sb.append(",");
        if (getPrice() != null) {
            sb.append(getPrice());
        }
        sb.append(",");
        if (getQuantity() != null) {
            sb.append(getQuantity());
        }
        sb.append(",");
        if (getOrigType() != null) {
            sb.append(getOrigType());
        }
        sb.append(",");
        if (getType() != null) {
            sb.append(getType());
        }
        sb.append(",");
        if (getTimeInForce() != null) {
            sb.append(getTimeInForce());
        }
        sb.append(",");
        if (getOrderId() != null) {
            sb.append(getOrderId());
        }
        sb.append(",");
        if (getClientOrderId() != null) {
            sb.append(getClientOrderId());
        }
        sb.append(",");
        if (getReduceOnly() != null) {
            sb.append(getReduceOnly());
        }
        sb.append(",");
        if (getWorkingType() != null) {
            sb.append(getWorkingType());
        }
        sb.append(",");
        if (getStopPrice() != null) {
            sb.append(getStopPrice());
        }
        sb.append(",");
        if (getClosePosition() != null) {
            sb.append(getClosePosition());
        }
        sb.append(",");
        if (getActivationPrice() != null) {
            sb.append(getActivationPrice());
        }
        sb.append(",");
        if (getCallbackRate() != null) {
            sb.append(getCallbackRate());
        }
        sb.append(",");
        if (getPriceProtect() != null) {
            sb.append(getPriceProtect());
        }
        sb.append(",");
        if (getOrderTime() != null) {
            sb.append(getOrderTime());
        }
        sb.append(",");
        if (getUpdateTime() != null) {
            sb.append(getUpdateTime());
        }
        sb.append(",");
        if (getFrozenFee() != null) {
            sb.append(getFrozenFee());
        }
        sb.append(",");
        if (getFrozenMargin() != null) {
            sb.append(getFrozenMargin());
        }
        sb.append(",");
        if (getCumQuote() != null) {
            sb.append(getCumQuote());
        }
        sb.append(",");
        if (getExecutedQty() != null) {
            sb.append(getExecutedQty());
        }
        sb.append(",");
        if (getAvgPrice() != null) {
            sb.append(getAvgPrice());
        }
        sb.append(",");
        if (getFee() != null) {
            sb.append(getFee());
        }
        sb.append(",");
        if (getFeeAsset() != null) {
            sb.append(getFeeAsset());
        }
        sb.append(",");
        if (getCloseProfit() != null) {
            sb.append(getCloseProfit());
        }
        sb.append(",");
        if (getLeverage() != null) {
            sb.append(getLeverage());
        }
        sb.append(",");
        if (getLeftQty() != null) {
            sb.append(getLeftQty());
        }
        sb.append(",");
        if (getMarginType() != null) {
            sb.append(getMarginType());
        }
        sb.append(",");
        if (getFirstIsolatedOrder() != null) {
            sb.append(getFirstIsolatedOrder());
        }
        sb.append(",");
        if (getOtoOrderType() != null) {
            sb.append(getOtoOrderType());
        }
        sb.append(",");
        if (getSubOrderId1() != null) {
            sb.append(getSubOrderId1());
        }
        sb.append(",");
        if (getSubOrderId2() != null) {
            sb.append(getSubOrderId2());
        }
        return sb.toString();
    }

    public static Order toObject(String str) {
        Order obj = new Order();
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
            obj.setSide(OrderSide.valueOf(values[4]));
        }

        if (!values[5].equals("")) {
            obj.setPositionSide(PositionSide.valueOf(values[5]));
        }

        if (!values[6].equals("")) {
            obj.setStatus(OrderStatus.valueOf(values[6]));
        }

        if (!values[7].equals("")) {
            obj.setPrice(new BigDecimal(values[7]));
        }

        if (!values[8].equals("")) {
            obj.setQuantity(new BigDecimal(values[8]));
        }

        if (!values[9].equals("")) {
            obj.setOrigType(OrderType.valueOf(values[9]));
        }

        if (!values[10].equals("")) {
            obj.setType(OrderType.valueOf(values[10]));
        }

        if (!values[11].equals("")) {
            obj.setTimeInForce(TimeInForce.valueOf(values[11]));
        }

        if (!values[12].equals("")) {
            obj.setOrderId(Long.parseLong(values[12]));
        }

        if (!values[13].equals("")) {
            obj.setClientOrderId(values[13]);
        }

        if (!values[14].equals("")) {
            obj.setReduceOnly(Boolean.valueOf(values[14]));
        }

        if (!values[15].equals("")) {
            obj.setWorkingType(WorkingType.valueOf(values[15]));
        }

        if (!values[16].equals("")) {
            obj.setStopPrice(new BigDecimal(values[16]));
        }

        if (!values[17].equals("")) {
            obj.setClosePosition(Boolean.valueOf(values[17]));
        }

        if (!values[18].equals("")) {
            obj.setActivationPrice(new BigDecimal(values[18]));
        }

        if (!values[19].equals("")) {
            obj.setCallbackRate(new BigDecimal(values[19]));
        }

        if (!values[20].equals("")) {
            obj.setPriceProtect(Boolean.valueOf(values[20]));
        }

        if (!values[21].equals("")) {
            obj.setOrderTime(Long.parseLong(values[21]));
        }

        if (!values[22].equals("")) {
            obj.setUpdateTime(Long.parseLong(values[22]));
        }

        if (!values[23].equals("")) {
            obj.setFrozenFee(new BigDecimal(values[23]));
        }

        if (!values[24].equals("")) {
            obj.setFrozenMargin(new BigDecimal(values[24]));
        }

        if (!values[25].equals("")) {
            obj.setCumQuote(new BigDecimal(values[25]));
        }

        if (!values[26].equals("")) {
            obj.setExecutedQty(new BigDecimal(values[26]));
        }

        if (!values[27].equals("")) {
            obj.setAvgPrice(new BigDecimal(values[27]));
        }

        if (!values[28].equals("")) {
            obj.setFee(new BigDecimal(values[28]));
        }

        if (!values[29].equals("")) {
            obj.setFeeAsset(values[29]);
        }

        if (!values[30].equals("")) {
            obj.setCloseProfit(new BigDecimal(values[30]));
        }

        if (!values[31].equals("")) {
            obj.setLeverage(new BigDecimal(values[31]));
        }

        if (!values[32].equals("")) {
            obj.setLeftQty(new BigDecimal(values[32]));
        }

        if (!values[33].equals("")) {
            obj.setMarginType(MarginType.valueOf(values[33]));
        }

        if (!values[34].equals("")) {
            obj.setFirstIsolatedOrder(Boolean.valueOf(values[34]));
        }

        if (!values[35].equals("")) {
            obj.setOtoOrderType(OTOOrderType.valueOf(values[35]));
        }

        if (!values[36].equals("")) {
            obj.setSubOrderId1(Long.parseLong(values[36]));
        }

        if (!values[37].equals("")) {
            obj.setSubOrderId2(Long.parseLong(values[37]));
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

        if (getSide() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"side\":\"").append(getSide()).append("\"");
        }

        if (getPositionSide() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"positionSide\":\"").append(getPositionSide()).append("\"");
        }

        if (getStatus() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"status\":\"").append(getStatus()).append("\"");
        }

        if (getPrice() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"price\":").append(getPrice());
        }

        if (getQuantity() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"quantity\":").append(getQuantity());
        }

        if (getOrigType() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"origType\":\"").append(getOrigType()).append("\"");
        }

        if (getType() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"type\":\"").append(getType()).append("\"");
        }

        if (getTimeInForce() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"timeInForce\":\"").append(getTimeInForce()).append("\"");
        }

        if (getOrderId() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"orderId\":").append(getOrderId());
        }

        if (getClientOrderId() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"clientOrderId\":\"").append(getClientOrderId()).append("\"");
        }

        if (getReduceOnly() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"reduceOnly\":").append(getReduceOnly());
        }

        if (getWorkingType() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"workingType\":\"").append(getWorkingType()).append("\"");
        }

        if (getStopPrice() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"stopPrice\":").append(getStopPrice());
        }

        if (getClosePosition() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"closePosition\":").append(getClosePosition());
        }

        if (getActivationPrice() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"activationPrice\":").append(getActivationPrice());
        }

        if (getCallbackRate() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"callbackRate\":").append(getCallbackRate());
        }

        if (getPriceProtect() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"priceProtect\":").append(getPriceProtect());
        }

        if (getOrderTime() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"orderTime\":").append(getOrderTime());
        }

        if (getUpdateTime() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"updateTime\":").append(getUpdateTime());
        }

        if (getFrozenFee() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"frozenFee\":").append(getFrozenFee());
        }

        if (getFrozenMargin() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"frozenMargin\":").append(getFrozenMargin());
        }

        if (getCumQuote() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"cumQuote\":").append(getCumQuote());
        }

        if (getExecutedQty() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"executedQty\":").append(getExecutedQty());
        }

        if (getAvgPrice() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"avgPrice\":").append(getAvgPrice());
        }

        if (getFee() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"fee\":").append(getFee());
        }

        if (getFeeAsset() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"feeAsset\":\"").append(getFeeAsset()).append("\"");
        }

        if (getCloseProfit() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"closeProfit\":").append(getCloseProfit());
        }

        if (getLeverage() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"leverage\":").append(getLeverage());
        }

        if (getLeftQty() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"leftQty\":").append(getLeftQty());
        }

        if (getOtoOrderType() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"otoOrderType\":\"").append(getOtoOrderType()).append("\"");
        }

        if (getSubOrderId1() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"subOrderId1\":").append(getSubOrderId1());
        }

        if (getSubOrderId2() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"subOrderId2\":").append(getSubOrderId2());
        }

        sb.append("}");
        return sb.toString();
    }

}
