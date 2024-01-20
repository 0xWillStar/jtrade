package com.crypto.jtrade.core.provider.util;

import java.util.concurrent.atomic.AtomicLong;

import lombok.experimental.UtilityClass;

/**
 * sequence helper, include RequestId、OrderId、TradeId
 *
 * @author 0xWill
 **/
@UtilityClass
public class SequenceHelper {

    private AtomicLong requestId = new AtomicLong();

    private AtomicLong orderId = new AtomicLong();

    private AtomicLong tradeId = new AtomicLong();

    /**
     * Increments by one the current value and return the new value.
     */
    public Long incrementAndGetRequestId() {
        return requestId.incrementAndGet();
    }

    /**
     * Sets to the given value.
     */
    public void setRequestId(Long id) {
        requestId.set(id);
    }

    /**
     * Increments by one the current value and return the new value.
     */
    public Long incrementAndGetOrderId() {
        return orderId.incrementAndGet();
    }

    /**
     * Sets to the given value.
     */
    public void setOrderId(Long id) {
        orderId.set(id);
    }

    /**
     * Increments by one the current value and return the new value.
     */
    public Long incrementAndGetTradeId() {
        return tradeId.incrementAndGet();
    }

    /**
     * Sets to the given value.
     */
    public void setTradeId(Long id) {
        tradeId.set(id);
    }

    /**
     * get the current tradeId
     */
    public Long getTradeId() {
        return tradeId.get();
    }

}
