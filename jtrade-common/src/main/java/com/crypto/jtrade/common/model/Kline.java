package com.crypto.jtrade.common.model;

import java.math.BigDecimal;

import com.crypto.jtrade.common.annotation.MyField;
import com.crypto.jtrade.common.annotation.MyType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

/**
 * kline extension object
 *
 * @author 0xWill
 **/
@Data
@MyType(arrayFormat = true)
public class Kline {

    @MyField(json = false)
    private String symbol;

    @MyField(json = false)
    private String period;

    private Long beginTime;

    @MyField(json = false)
    private Long endTime;

    private BigDecimal openPrice = BigDecimal.ZERO;

    private BigDecimal highPrice = BigDecimal.ZERO;

    private BigDecimal lowPrice = BigDecimal.ZERO;

    private BigDecimal closePrice = BigDecimal.ZERO;

    private BigDecimal volume = BigDecimal.ZERO;

    private BigDecimal quoteVolume = BigDecimal.ZERO;

    private Integer count = 0;

    @MyField(text = false, json = false)
    @JsonIgnore
    private String tableName;

    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        if (getSymbol() != null) {
            sb.append(getSymbol());
        }
        sb.append(",");
        if (getPeriod() != null) {
            sb.append(getPeriod());
        }
        sb.append(",");
        if (getBeginTime() != null) {
            sb.append(getBeginTime());
        }
        sb.append(",");
        if (getEndTime() != null) {
            sb.append(getEndTime());
        }
        sb.append(",");
        if (getOpenPrice() != null) {
            sb.append(getOpenPrice());
        }
        sb.append(",");
        if (getHighPrice() != null) {
            sb.append(getHighPrice());
        }
        sb.append(",");
        if (getLowPrice() != null) {
            sb.append(getLowPrice());
        }
        sb.append(",");
        if (getClosePrice() != null) {
            sb.append(getClosePrice());
        }
        sb.append(",");
        if (getVolume() != null) {
            sb.append(getVolume());
        }
        sb.append(",");
        if (getQuoteVolume() != null) {
            sb.append(getQuoteVolume());
        }
        sb.append(",");
        if (getCount() != null) {
            sb.append(getCount());
        }
        return sb.toString();
    }

    public String toJSONString() {
        StringBuilder sb = new StringBuilder(512);
        sb.append("[");
        if (getBeginTime() != null) {
            sb.append(getBeginTime());
        }
        sb.append(",");
        if (getOpenPrice() != null) {
            sb.append(getOpenPrice());
        }
        sb.append(",");
        if (getHighPrice() != null) {
            sb.append(getHighPrice());
        }
        sb.append(",");
        if (getLowPrice() != null) {
            sb.append(getLowPrice());
        }
        sb.append(",");
        if (getClosePrice() != null) {
            sb.append(getClosePrice());
        }
        sb.append(",");
        if (getVolume() != null) {
            sb.append(getVolume());
        }
        sb.append(",");
        if (getQuoteVolume() != null) {
            sb.append(getQuoteVolume());
        }
        sb.append(",");
        if (getCount() != null) {
            sb.append(getCount());
        }
        sb.append("]");
        return sb.toString();
    }

}
