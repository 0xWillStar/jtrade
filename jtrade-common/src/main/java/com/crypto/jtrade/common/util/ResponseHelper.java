package com.crypto.jtrade.common.util;

import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.BaseResponse;

import lombok.experimental.UtilityClass;

/**
 * Response helper
 *
 * @author 0xWill
 */
@UtilityClass
public class ResponseHelper {

    public <T> BaseResponse<T> success() {
        return new BaseResponse<>();
    }

    public <T> BaseResponse<T> success(T data) {
        BaseResponse<T> response = new BaseResponse<T>();
        response.setData(data);
        return response;
    }

    public BaseResponse error(Integer code, String message) {
        BaseResponse response = new BaseResponse<>();
        response.setCode(code);
        response.setMsg(message);
        return response;
    }

    public BaseResponse error(TradeError error) {
        return error(error.getCode(), error.getMessage());
    }

    public BaseResponse error(TradeException exception) {
        return error(exception.getCode(), exception.getMessage());
    }
}
