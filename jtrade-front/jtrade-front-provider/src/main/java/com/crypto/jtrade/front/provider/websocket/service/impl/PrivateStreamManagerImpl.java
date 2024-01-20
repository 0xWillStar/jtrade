package com.crypto.jtrade.front.provider.websocket.service.impl;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.util.concurrent.ConcurrentHashSet;
import com.crypto.jtrade.front.provider.config.FrontConfig;
import com.crypto.jtrade.front.provider.model.StreamRequestArg;
import com.crypto.jtrade.front.provider.websocket.service.PrivateStreamManager;
import com.crypto.jtrade.front.provider.websocket.service.StreamWorker;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * private stream manager
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class PrivateStreamManagerImpl extends AbstractStreamManager implements PrivateStreamManager {

    @Autowired
    private FrontConfig frontConfig;

    @Getter
    private final boolean privateStreamManager = true;

    @Getter
    private final String threadFactoryPrefix = "jtrade-private-stream-disruptor-";

    // KEY: sessionId, VALUE: clientId
    private final ConcurrentHashMap<String, String> sessionClientMap = new ConcurrentHashMap<>(1024);

    // KEY: clientId
    private final ConcurrentHashMap<String, ClientContext> clientWorkerMap = new ConcurrentHashMap<>(1024);

    /**
     * get size of stream worker
     */
    @Override
    protected Integer getStreamWorkerSize() {
        return frontConfig.getPrivateStreamWorkerSize();
    }

    /**
     * get topic string
     */
    @Override
    protected String getTopicString(String sessionId, StreamRequestArg argument) {
        String clientId = sessionClientMap.get(sessionId);
        if (clientId == null) {
            log.error("the client of the websocket session id({}) is not found", sessionId);
            return null;
        } else {
            return argument.toTopicString(clientId);
        }
    }

    /**
     * get worker id by the session id
     */
    @Override
    protected Integer getWorkerId(String sessionId) {
        int workerId = 0;
        String clientId = sessionClientMap.get(sessionId);
        if (clientId == null) {
            log.warn("the client of the websocket session id({}) is not found", sessionId);
        } else {
            ClientContext context = clientWorkerMap.get(clientId);
            if (context == null) {
                log.warn("the websocket stream worker of the client id({}) is not found", clientId);
            } else {
                workerId = context.getWorkerId();
            }
        }
        if (workerId == 0) {
            int hashCode = Math.abs(sessionId.hashCode());
            workerId = hashCode % getStreamWorkerSize() + 1;
        }
        return workerId;
    }

    /**
     * remove channel
     */
    @Override
    public void removeChannel(String sessionId) {
        super.removeChannel(sessionId);
        // clear local cache
        String clientId = sessionClientMap.get(sessionId);
        if (clientId != null) {
            ClientContext context = clientWorkerMap.get(clientId);
            if (context != null) {
                context.getSessionIds().remove(sessionId);
                if (context.getSessionIds().size() == 0) {
                    clientWorkerMap.remove(clientId);
                }
            }
            sessionClientMap.remove(sessionId);
        }
    }

    /**
     * push data to websocket client
     */
    @Override
    public void push(String clientId, String topic, String data) {
        ClientContext context = clientWorkerMap.get(clientId);
        if (context == null) {
            log.error("the websocket stream worker of the client id({}) is not found", clientId);
        } else {
            pushToWorker(context.getWorkerId(), topic, data);
        }
    }

    /**
     * adjust worker
     */
    @Override
    protected void adjustWorker(String sessionId, Channel channel, String clientId, int workerId) {
        ClientContext context = clientWorkerMap.get(clientId);
        if (context == null) {
            ConcurrentHashSet<String> sessionIds = new ConcurrentHashSet<>();
            sessionIds.add(sessionId);
            context = new ClientContext(workerId, sessionIds);
            clientWorkerMap.put(clientId, context);
            sessionClientMap.put(sessionId, clientId);

        } else {
            int origWorkerId = context.getWorkerId();
            if (workerId != origWorkerId) {
                // remove from current worker
                StreamWorker worker = streamWorkerMap.get(workerId);
                worker.remove(sessionId);
                // add to original worker
                StreamWorker origWorker = streamWorkerMap.get(origWorkerId);
                origWorker.add(sessionId, channel);
            }
            context.getSessionIds().add(sessionId);
            sessionClientMap.put(sessionId, clientId);
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private static class ClientContext {

        private Integer workerId;

        private ConcurrentHashSet<String> sessionIds;
    }

}
