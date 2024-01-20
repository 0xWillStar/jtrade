package com.crypto.jtrade.front.provider.model;

import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.front.provider.constants.StreamOp;

import lombok.Data;

/**
 * stream response
 *
 * @author 0xWill
 **/
@Data
public class StreamResponse {

    private StreamOp event;

    private StreamRequestArg arg;

    private Integer code;

    private String msg;

    public void setError(TradeError error) {
        this.setCode(error.getCode());
        this.setMsg(error.getMessage());
    }

}
