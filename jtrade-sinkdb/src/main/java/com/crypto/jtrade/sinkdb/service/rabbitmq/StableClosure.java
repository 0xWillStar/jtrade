package com.crypto.jtrade.sinkdb.service.rabbitmq;

/**
 * callback closure
 *
 * @author 0xWill
 **/
public interface StableClosure {

    /**
     * called when the worker has finished processing this batch.
     */
    void finishAt(long batchId, int workerId);

}
