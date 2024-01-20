package com.crypto.jtrade.core.provider.util;

import java.util.concurrent.TimeUnit;

import org.springframework.web.context.request.async.DeferredResult;

import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.util.ResponseHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.experimental.UtilityClass;

/**
 * DeferredResult helper class
 *
 * @author 0xWill
 **/
@UtilityClass
public class DeferredResultHelper {

    private static Long DEFAULT_TIME_OUT = 5 * 1000L;

    private Cache<Long, DeferredResult<BaseResponse>> resultCache =
        CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).build();

    /**
     * generate a DeferredResult instance
     */
    public DeferredResult<BaseResponse> generateDeferredResult() {
        return new DeferredResult<>(DEFAULT_TIME_OUT, ResponseHelper.error(TradeError.REQUEST_PROCESS_TIMEOUT));
    }

    /**
     * register a DeferredResult instance
     */
    public void registerDeferredResult(Long requestId, DeferredResult<BaseResponse> result) {
        resultCache.put(requestId, result);
    }

    /**
     * release a DeferredResult instance
     */
    public void releaseDeferredResult(Long requestId, BaseResponse response) {
        DeferredResult<BaseResponse> result = resultCache.getIfPresent(requestId);
        if (result != null) {
            result.setResult(response);
        }
    }

}
