package com.crypto.jtrade.core.provider.model.liquidation;

import java.util.concurrent.CountDownLatch;

import com.crypto.jtrade.common.model.Order;
import com.alibaba.fastjson.annotation.JSONField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * canceled order when liquidation
 *
 * @author 0xWill
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiquidationCanceledOrder {

    @JSONField(serialize = false)
    private CountDownLatch latch;

    private Order order;

}
