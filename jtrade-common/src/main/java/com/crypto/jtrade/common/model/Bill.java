package com.crypto.jtrade.common.model;

import java.math.BigDecimal;

import com.crypto.jtrade.common.annotation.MyField;
import com.crypto.jtrade.common.annotation.MyType;
import com.crypto.jtrade.common.constants.BillType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * bill record
 *
 * @author 0xWill
 **/
@Data
@MyType(table = "t_bill")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Bill {

    @MyField(json = false)
    @JsonIgnore
    private String exchangeId;

    @MyField(json = false)
    @JsonIgnore
    private String memberId;

    @JsonIgnore
    private String clientId;

    private String symbol;

    private BillType billType;

    private String asset;

    private BigDecimal amount;

    private String info;

    @JsonIgnore
    private String correlationId;

    private Long insertTime;

    public static Bill createBill(String clientId, String symbol, BillType billType, String asset, BigDecimal amount,
        String correlationId) {
        Bill bill = new Bill();
        bill.setClientId(clientId);
        bill.setSymbol(symbol);
        bill.setBillType(billType);
        bill.setAsset(asset);
        bill.setAmount(amount);
        bill.setCorrelationId(correlationId);
        bill.setInsertTime(System.currentTimeMillis());
        return bill;
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
        if (getBillType() != null) {
            sb.append(getBillType());
        }
        sb.append(",");
        if (getAsset() != null) {
            sb.append(getAsset());
        }
        sb.append(",");
        if (getAmount() != null) {
            sb.append(getAmount());
        }
        sb.append(",");
        if (getInfo() != null) {
            sb.append(getInfo());
        }
        sb.append(",");
        if (getCorrelationId() != null) {
            sb.append(getCorrelationId());
        }
        sb.append(",");
        if (getInsertTime() != null) {
            sb.append(getInsertTime());
        }
        return sb.toString();
    }

}
