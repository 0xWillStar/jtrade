package com.crypto.jtrade.core.provider.service.trade.impl;

import com.alibaba.fastjson.JSON;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.util.LogExceptionHandler;
import com.crypto.jtrade.common.util.ResponseHelper;
import com.crypto.jtrade.core.provider.model.queue.CommandEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * Disruptor exception handler for command.
 *
 * @author 0xWill
 */
@Slf4j
public class CommandLogExceptionHandler<T extends CommandEvent> extends LogExceptionHandler<T> {

    public CommandLogExceptionHandler(String name) {
        super(name);
    }

    @Override
    public void handleEventException(Throwable ex, long sequence, T event) {
        log.error("Handle {} disruptor event error, event is {}", this.name, JSON.toJSONString(event), ex);
        if (event.getFuture() != null) {
            BaseResponse response;
            if (ex instanceof TradeException) {
                response = ResponseHelper.error((TradeException)ex);
            } else {
                response = ResponseHelper.error(TradeError.INTERNAL);
            }
            event.getFuture().complete(response);
        }
    }
}
