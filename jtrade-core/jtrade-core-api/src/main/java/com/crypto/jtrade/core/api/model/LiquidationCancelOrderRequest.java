package com.crypto.jtrade.core.api.model;

import java.util.concurrent.CountDownLatch;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Data;

/**
 * cancel order request when liquidation
 *
 * @author 0xWill
 **/
@Data
public class LiquidationCancelOrderRequest extends CancelOrderRequest {

    private Long expectedTradeId;

    @JSONField(serialize = false)
    private CountDownLatch latch;

}
