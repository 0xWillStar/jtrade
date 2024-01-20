package com.crypto.jtrade.common.model;

import java.math.BigDecimal;
import java.util.List;

import com.crypto.jtrade.common.annotation.MyField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * depth
 *
 * @author 0xWill
 **/
@Data
@NoArgsConstructor
public class Depth {

    private String symbol;

    @MyField(reference = true, arrayFormat = true)
    private List<Item> bids;

    @MyField(reference = true, arrayFormat = true)
    private List<Item> asks;

    private Long time = System.currentTimeMillis();

    public Depth(String symbol, List<Item> bids, List<Item> asks) {
        this.symbol = symbol;
        this.bids = bids;
        this.asks = asks;
    }

    public String toJSONString() {
        StringBuilder sb = new StringBuilder(1024);
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

        if (getBids() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"bids\":[");
            for (int i = 0; i < getBids().size(); i++) {
                Item item = getBids().get(i);
                sb.append("[");
                if (item.getPrice() != null) {
                    sb.append(item.getPrice());
                }
                sb.append(",");
                if (item.getQuantity() != null) {
                    sb.append(item.getQuantity());
                }
                sb.append("]");
                if (i < getBids().size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
        }

        if (getAsks() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"asks\":[");
            for (int i = 0; i < getAsks().size(); i++) {
                Item item1 = getAsks().get(i);
                sb.append("[");
                if (item1.getPrice() != null) {
                    sb.append(item1.getPrice());
                }
                sb.append(",");
                if (item1.getQuantity() != null) {
                    sb.append(item1.getQuantity());
                }
                sb.append("]");
                if (i < getAsks().size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
        }

        if (getTime() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"time\":").append(getTime());
        }

        sb.append("}");
        return sb.toString();
    }

    @Data
    @AllArgsConstructor
    public static final class Item {

        private BigDecimal price;

        private BigDecimal quantity;
    }

}
