package com.crypto.jtrade.sinkdb.model;

import java.io.Serializable;
import java.util.List;

import com.crypto.jtrade.sinkdb.service.rabbitmq.StableClosure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * batch entity transferred in queue
 *
 * @author 0xWill
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchEntity implements Serializable {

    private static final long serialVersionUID = 1412263390712547929L;

    private Long batchId;

    private List<String> messageList;

    private StableClosure stableClosure;

}
