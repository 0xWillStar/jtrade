package com.crypto.jtrade.core.api.model;

import java.util.concurrent.CountDownLatch;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * empty request
 *
 * @author 0xWill
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmptyRequest {

    private String symbol;

    @JSONField(serialize = false)
    private CountDownLatch latch;

}
