package com.crypto.jtrade.common.model;

import java.math.BigDecimal;

import com.crypto.jtrade.common.util.BigDecimalUtil;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ticker
 *
 * @author 0xWill
 **/
@Data
@NoArgsConstructor
public class Ticker {

    private String symbol;

    private BigDecimal priceChange = BigDecimal.ZERO;

    private BigDecimal priceChangePercent = BigDecimal.ZERO;

    private BigDecimal lastPrice = BigDecimal.ZERO;

    private BigDecimal lastQty = BigDecimal.ZERO;

    private BigDecimal openPrice = BigDecimal.ZERO;

    private BigDecimal highPrice = BigDecimal.ZERO;

    private BigDecimal lowPrice = BigDecimal.ZERO;

    private BigDecimal volume = BigDecimal.ZERO;

    private BigDecimal quoteVolume = BigDecimal.ZERO;

    private Integer count = 0;

    public Ticker(String symbol) {
        this.symbol = symbol;
    }

    public void clear() {
        this.setPriceChange(BigDecimal.ZERO);
        this.setPriceChangePercent(BigDecimal.ZERO);
        this.setOpenPrice(BigDecimal.ZERO);
        this.setHighPrice(BigDecimal.ZERO);
        this.setLowPrice(BigDecimal.ZERO);
        this.setVolume(BigDecimal.ZERO);
        this.setQuoteVolume(BigDecimal.ZERO);
        this.setCount(0);
    }

    public void refreshPriceChange() {
        this.setPriceChange(this.lastPrice.subtract(this.openPrice));
        if (this.openPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal percent = BigDecimalUtil.getVal(BigDecimalUtil.divide(this.getPriceChange(), this.openPrice), 4);
            this.setPriceChangePercent(percent);
        }
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

        if (getPriceChange() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"priceChange\":").append(getPriceChange());
        }

        if (getPriceChangePercent() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"priceChangePercent\":").append(getPriceChangePercent());
        }

        if (getLastPrice() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"lastPrice\":").append(getLastPrice());
        }

        if (getLastQty() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"lastQty\":").append(getLastQty());
        }

        if (getOpenPrice() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"openPrice\":").append(getOpenPrice());
        }

        if (getHighPrice() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"highPrice\":").append(getHighPrice());
        }

        if (getLowPrice() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"lowPrice\":").append(getLowPrice());
        }

        if (getVolume() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"volume\":").append(getVolume());
        }

        if (getQuoteVolume() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"quoteVolume\":").append(getQuoteVolume());
        }

        if (getCount() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"count\":").append(getCount());
        }

        sb.append("}");
        return sb.toString();
    }

}
