package com.crypto.jtrade.core.api.model;

import java.math.BigDecimal;

import com.crypto.jtrade.common.model.MarkPrice;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * mark price request
 *
 * @author 0xWill
 */
@Data
@NoArgsConstructor
public class MarkPriceRequest extends MarkPrice {

    public MarkPriceRequest(String symbol, BigDecimal markPrice, Long time) {
        setSymbol(symbol);
        setMarkPrice(markPrice);
        setTime(time);
    }

}
