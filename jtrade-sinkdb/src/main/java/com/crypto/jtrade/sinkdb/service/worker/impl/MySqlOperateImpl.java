package com.crypto.jtrade.sinkdb.service.worker.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.DataAction;
import com.crypto.jtrade.common.constants.DataObject;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.sinkdb.model.Accomplish;
import com.crypto.jtrade.sinkdb.service.worker.MySqlOperate;
import com.crypto.jtrade.sinkdb.service.worker.operation.TableOperation;

import lombok.extern.slf4j.Slf4j;

/**
 * execute sql in MySQL
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class MySqlOperateImpl implements MySqlOperate {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private List<TableOperation> operationList;

    private Map<DataObject, TableOperation> operationMap;

    @PostConstruct
    public void init() {
        operationMap =
            operationList.stream().collect(Collectors.toMap(TableOperation::getDataObject, operation -> operation));
    }

    /**
     * batching execute
     */
    @Override
    @Transactional
    public void batchExecute(int workerId, long batchId, List<String> messages) {
        List<String> sqlList = new ArrayList<>(messages.size() << 1);
        for (String message : messages) {
            String[] strArr = splitMessage(message);
            if (strArr[0].equals(Constants.EMPTY) || strArr[1].equals(Constants.EMPTY)
                || strArr[2].equals(Constants.EMPTY)) {
                continue;
            }

            DataObject dataObject = DataObject.valueOf(strArr[0]);
            DataAction dataAction = DataAction.valueOf(strArr[1]);
            TableOperation tableOperation = operationMap.get(dataObject);
            if (tableOperation == null) {
                throw new TradeException(TradeError.DATA_OBJECT_INVALID);
            }
            String data = strArr[2];
            switch (dataAction) {
                case INSERT:
                    sqlList.add(tableOperation.insert(data));
                    break;
                case UPDATE:
                    sqlList.add(tableOperation.update(data));
                    break;
                case DELETE:
                    sqlList.add(tableOperation.delete(data));
                    break;
                case UNKNOWN:
                    sqlList.add(tableOperation.delete(data));
                    sqlList.add(tableOperation.insert(data));
                    break;
                default:
                    throw new TradeException(TradeError.DATA_ACTION_INVALID);
            }

            if (dataObject == DataObject.ORDER && dataAction == DataAction.DELETE) {
                TableOperation finishOrderOperation = operationMap.get(DataObject.FINISH_ORDER);
                sqlList.add(finishOrderOperation.insert(data));
            }
        }
        Accomplish accomplish = new Accomplish(workerId, batchId);
        sqlList.add(accomplish.getUpdateSql());
        jdbcTemplate.batchUpdate(sqlList.toArray(new String[0]));
    }

    /**
     * split the message into 3 parts Messages are separated by commas: the first part is DataObject, the second part is
     * DataAction, the third part is the remainder.
     */
    private String[] splitMessage(String message) {
        int len = message.length();
        int i = 0;
        int start = 0;

        int j = 0;
        String dataObject = Constants.EMPTY;
        String dataAction = Constants.EMPTY;
        String remainder = Constants.EMPTY;
        while (i < len) {
            if (message.charAt(i) == Constants.COMMA) {
                String value = message.substring(start, i);
                if (j == 0) {
                    dataObject = value;
                    j++;
                } else if (j == 1) {
                    dataAction = value;
                    remainder = message.substring(i + 1);
                    break;
                }
                start = i + 1;
            }
            i++;
        }
        return new String[] {dataObject, dataAction, remainder};
    }

}
