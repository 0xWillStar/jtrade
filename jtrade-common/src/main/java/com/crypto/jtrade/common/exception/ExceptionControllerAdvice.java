package com.crypto.jtrade.common.exception;

import static javax.servlet.http.HttpServletResponse.SC_OK;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.util.ResponseHelper;

import lombok.extern.slf4j.Slf4j;

/**
 * Unified exception interception
 *
 * @author 0xWill
 */
@ConditionalOnProperty(name = "controller.advice.on", havingValue = "true", matchIfMissing = true)
@ControllerAdvice
@Slf4j
public class ExceptionControllerAdvice {

    @ExceptionHandler(TradeException.class)
    @ResponseBody
    public BaseResponse tradeExceptionHandler(HttpServletRequest request, HttpServletResponse response,
        TradeException tradeException) {
        log.error("Advice handle trade exception", tradeException);
        response.setStatus(SC_OK);
        return ResponseHelper.error(tradeException);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public BaseResponse validExceptionHandler(HttpServletRequest request, HttpServletResponse response,
        MethodArgumentNotValidException validException) {
        log.error("Advice handle valid exception", validException);
        response.setStatus(SC_OK);
        return ResponseHelper.error(TradeError.ARGUMENT_INVALID.getCode(),
            validException.getBindingResult().getFieldError().getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public BaseResponse handleError(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        log.error("Advice handle global exception", exception);
        response.setStatus(SC_OK);
        return ResponseHelper.error(TradeError.INTERNAL);
    }

}
