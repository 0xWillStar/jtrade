package com.crypto.jtrade.sinkdb.service.worker;

import java.util.List;

import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.sinkdb.service.rabbitmq.StableClosure;

/**
 * mysql worker
 *
 * @author 0xWill
 **/
public interface MySqlWorker {

    /**
     * init the worker
     */
    void init(int workerId, long lastBatchId, NamedThreadFactory threadFactory);

    /**
     * append entity
     */
    void appendEntity(long batchId, List<String> messages, StableClosure stableClosure);

}
