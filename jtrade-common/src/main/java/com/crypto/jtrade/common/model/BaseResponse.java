package com.crypto.jtrade.common.model;

import java.io.Serializable;

import com.crypto.jtrade.common.exception.TradeError;

import lombok.Data;

/**
 * Response base class
 *
 * @author 0xWill
 **/
@Data
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 2200364209317890819L;

    private Integer code = TradeError.SUCCESS.getCode();

    private String msg = TradeError.SUCCESS.getMessage();

    private Long ts = System.currentTimeMillis();

    private T data;

    public static <T> BaseResponse<T> of(T data) {
        BaseResponse<T> baseResponse = new BaseResponse<>();
        baseResponse.setData(data);
        return baseResponse;
    }

    public boolean isError() {
        return !TradeError.SUCCESS.getCode().equals(code);
    }

}
