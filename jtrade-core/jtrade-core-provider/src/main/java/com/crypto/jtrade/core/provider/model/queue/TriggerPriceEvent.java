package com.crypto.jtrade.core.provider.model.queue;

import java.io.Serializable;
import java.math.BigDecimal;

import com.crypto.jtrade.common.constants.WorkingType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * trigger price for stop order
 *
 * @author 0xWill
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriggerPriceEvent implements Serializable {

    private static final long serialVersionUID = -8837590862492612069L;

    private WorkingType workingType;

    private String symbol;

    private BigDecimal price;

}
