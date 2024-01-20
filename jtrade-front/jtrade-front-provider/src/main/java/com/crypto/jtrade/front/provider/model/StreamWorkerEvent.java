package com.crypto.jtrade.front.provider.model;

import java.io.Serializable;

import com.alibaba.fastjson.annotation.JSONField;
import com.crypto.jtrade.front.provider.constants.StreamCommandIdentity;

import io.netty.channel.Channel;
import lombok.Data;

/**
 * stream worker event
 *
 * @author 0xWill
 **/
@Data
public class StreamWorkerEvent implements Serializable {

    private static final long serialVersionUID = 7160864150942331513L;

    private StreamCommandIdentity identity;

    private String sessionId;

    private String topic;

    private String data;

    @JSONField(serialize = false)
    private Channel channel;

}
