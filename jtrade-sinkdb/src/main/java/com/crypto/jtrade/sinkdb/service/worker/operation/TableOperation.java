package com.crypto.jtrade.sinkdb.service.worker.operation;

import com.crypto.jtrade.common.constants.DataObject;

/**
 * table operation
 *
 * @author 0xWill
 */
public interface TableOperation {

    DataObject getDataObject();

    String insert(String data);

    String update(String data);

    String delete(String data);

}
