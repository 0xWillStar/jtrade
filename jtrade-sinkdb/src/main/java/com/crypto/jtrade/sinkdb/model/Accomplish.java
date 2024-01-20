package com.crypto.jtrade.sinkdb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Record data synchronization status
 *
 * @author 0xWill
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Accomplish {

    private Integer workerId;

    private Long batchId;

    public String getUpdateSql() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("UPDATE t_accomplish SET batch_id=").append(this.batchId).append(" WHERE worker_id=")
            .append(this.workerId);
        return sb.toString();
    }

}
