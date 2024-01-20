package com.crypto.jtrade.core.provider.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import com.crypto.jtrade.common.model.BaseResponse;

import lombok.experimental.UtilityClass;

/**
 * response future helper
 *
 * @author 0xWill
 **/
@UtilityClass
public class ResponseFutureHelper {

    private ConcurrentHashMap<Long, CompletableFuture<BaseResponse>> futureCache = new ConcurrentHashMap<>();

    /**
     * The request is only processed in one thread, and there is only one future at any time.
     */
    private CompletableFuture<BaseResponse> responseFuture;

    /**
     * generate a future instance
     */
    public CompletableFuture<BaseResponse> generateFuture() {
        return new CompletableFuture<>();
    }

    /**
     * register a future instance
     */
    public void registerFuture(Long requestId, CompletableFuture<BaseResponse> future) {
        futureCache.put(requestId, future);
    }

    /**
     * register a future instance
     */
    public void registerFuture(CompletableFuture<BaseResponse> future) {
        responseFuture = future;
    }

    /**
     * release a future instance
     */
    public void releaseFuture(Long requestId, BaseResponse response) {
        CompletableFuture<BaseResponse> future = futureCache.get(requestId);
        if (future != null) {
            future.complete(response);
            futureCache.remove(requestId);
        }
    }

    /**
     * release a future instance
     */
    public void releaseFuture(BaseResponse response) {
        if (responseFuture != null) {
            responseFuture.complete(response);
            responseFuture = null;
        }
    }

}
