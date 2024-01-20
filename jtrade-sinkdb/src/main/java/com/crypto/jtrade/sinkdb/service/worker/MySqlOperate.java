package com.crypto.jtrade.sinkdb.service.worker;

import java.util.List;

/**
 * execute sql in MySQL
 *
 * @author 0xWill
 **/
public interface MySqlOperate {

    /**
     * batching execute
     */
    void batchExecute(int workerId, long batchId, List<String> messages);

}
