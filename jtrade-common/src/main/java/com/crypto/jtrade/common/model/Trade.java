package com.crypto.jtrade.common.model;

import java.math.BigDecimal;

import com.crypto.jtrade.common.annotation.MyField;
import com.crypto.jtrade.common.annotation.MyType;
import com.crypto.jtrade.common.constants.MatchRole;
import com.crypto.jtrade.common.constants.OrderSide;
import com.crypto.jtrade.common.constants.PositionSide;
import com.crypto.jtrade.common.constants.TradeType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

/**
 * Trade information
 *
 * @author 0xWill
 **/
@Data
@Builder
@MyType(table = "t_trade")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Trade {

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
    private Long tradeId;

    @MyField(key = true)
    private OrderSide side;

    private PositionSide positionSide;

    private Long orderId;

    private String clientOrderId;

    private BigDecimal price;

    private BigDecimal qty;

    private BigDecimal quoteQty;

    private BigDecimal closeProfit;

    private BigDecimal fee;

    private String feeAsset;

    private MatchRole matchRole;

    private Long tradeTime;

    private TradeType tradeType;

    private BigDecimal liquidationPrice;

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
        if (getTradeId() != null) {
            sb.append(getTradeId());
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
        if (getOrderId() != null) {
            sb.append(getOrderId());
        }
        sb.append(",");
        if (getClientOrderId() != null) {
            sb.append(getClientOrderId());
        }
        sb.append(",");
        if (getPrice() != null) {
            sb.append(getPrice());
        }
        sb.append(",");
        if (getQty() != null) {
            sb.append(getQty());
        }
        sb.append(",");
        if (getQuoteQty() != null) {
            sb.append(getQuoteQty());
        }
        sb.append(",");
        if (getCloseProfit() != null) {
            sb.append(getCloseProfit());
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
        if (getMatchRole() != null) {
            sb.append(getMatchRole());
        }
        sb.append(",");
        if (getTradeTime() != null) {
            sb.append(getTradeTime());
        }
        sb.append(",");
        if (getTradeType() != null) {
            sb.append(getTradeType());
        }
        sb.append(",");
        if (getLiquidationPrice() != null) {
            sb.append(getLiquidationPrice());
        }
        return sb.toString();
    }

    public String toJSONString() {
        StringBuilder sb = new StringBuilder(512);
        boolean first = true;
        sb.append("{");
        if (getSymbol() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"symbol\":\"").append(getSymbol()).append("\"");
        }

        if (getTradeId() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"tradeId\":").append(getTradeId());
        }

        if (getSide() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"side\":\"").append(getSide()).append("\"");
        }

        if (getPrice() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"price\":").append(getPrice());
        }

        if (getQty() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"qty\":").append(getQty());
        }

        if (getMatchRole() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"matchRole\":\"").append(getMatchRole()).append("\"");
        }

        if (getTradeTime() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"tradeTime\":").append(getTradeTime());
        }

        sb.append("}");
        return sb.toString();
    }

}
