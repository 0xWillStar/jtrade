package com.crypto.jtrade.sinkdb.service.worker;

/**
 * worker manager
 *
 * @author 0xWill
 **/
public interface WorkerManager {

    /**
     * get mysql worker by the clientId
     */
    MySqlWorker getMySqlWorker(String clientId);

}
