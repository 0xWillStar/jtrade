package com.crypto.jtrade.front.provider.websocket.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.front.provider.config.FrontConfig;
import com.crypto.jtrade.front.provider.model.StreamRequestArg;
import com.crypto.jtrade.front.provider.websocket.service.PublicStreamManager;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * public stream manager
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class PublicStreamManagerImpl extends AbstractStreamManager implements PublicStreamManager {

    @Autowired
    private FrontConfig frontConfig;

    @Getter
    private final boolean privateStreamManager = false;

    @Getter
    private final String threadFactoryPrefix = "jtrade-public-stream-disruptor-";

    /**
     * get size of stream worker
     */
    @Override
    protected Integer getStreamWorkerSize() {
        return frontConfig.getPublicStreamWorkerSize();
    }

    /**
     * get topic string
     */
    @Override
    protected String getTopicString(String sessionId, StreamRequestArg argument) {
        return argument.toTopicString();
    }

    /**
     * get worker id by the session id
     */
    @Override
    protected Integer getWorkerId(String sessionId) {
        int hashCode = Math.abs(sessionId.hashCode());
        return hashCode % getStreamWorkerSize() + 1;
    }

    /**
     * push data to websocket client
     */
    @Override
    public void push(String topic, String data) {
        pushToAllWorkers(topic, data);
    }

}
