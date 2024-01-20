package com.crypto.jtrade.front.provider.websocket.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.crypto.jtrade.common.util.DisruptorBuilder;
import com.crypto.jtrade.common.util.LogExceptionHandler;
import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.front.provider.constants.Constants;
import com.crypto.jtrade.front.provider.constants.StreamCommandIdentity;
import com.crypto.jtrade.front.provider.model.StreamWorkerEvent;
import com.crypto.jtrade.front.provider.websocket.service.StreamWorker;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * websocket stream worker service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class StreamWorkerImpl implements StreamWorker {

    private static final int MAX_MISSING_PONGS = 3;

    @Value("${jtrade.disruptor.stream-worker-buffer-size:4096}")
    private Integer streamWorkerBufferSize;

    @Getter
    private int workerId;

    // KEY: sessionId
    private final Map<String, SessionContext> sessionMap = new HashMap<>(512);

    // KEY: topic, VALUE: sessionId
    private final Map<String, Set<String>> topicMap = new HashMap<>(512);

    private Disruptor<StreamWorkerEvent> workerDisruptor;

    private RingBuffer<StreamWorkerEvent> workerQueue;

    /**
     * init the streamWorker
     */
    @Override
    public void init(int workerId, NamedThreadFactory threadFactory) {
        this.workerId = workerId;

        this.workerDisruptor =
            DisruptorBuilder.<StreamWorkerEvent>newInstance().setRingBufferSize(streamWorkerBufferSize)
                .setEventFactory(new StreamWorkerFactory()).setThreadFactory(threadFactory)
                .setProducerType(ProducerType.MULTI).setWaitStrategy(new BlockingWaitStrategy()).build();
        this.workerDisruptor.handleEventsWith(new StreamWorkerHandler());
        this.workerDisruptor.setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
        this.workerQueue = this.workerDisruptor.start();
    }

    /**
     * A new channel is connected.
     */
    @Override
    public void add(String sessionId, Channel channel) {
        publishToQueue(StreamCommandIdentity.ADD, sessionId, null, null, channel);
    }

    /**
     * Send ping to all active channels regularly.
     */
    @Override
    public void ping() {
        publishToQueue(StreamCommandIdentity.PING, null, null, null, null);
    }

    /**
     * Received pong from a channel.
     */
    @Override
    public void pong(String sessionId) {
        publishToQueue(StreamCommandIdentity.PONG, sessionId, null, null, null);
    }

    /**
     * A channel subscribes topics.
     */
    @Override
    public void subscribe(String sessionId, String topic) {
        publishToQueue(StreamCommandIdentity.SUBSCRIBE, sessionId, topic, null, null);
    }

    /**
     * A channel unsubscribes topics.
     */
    @Override
    public void unsubscribe(String sessionId, String topic) {
        publishToQueue(StreamCommandIdentity.UNSUBSCRIBE, sessionId, topic, null, null);
    }

    /**
     * Response to subscribe or unsubscribe
     */
    @Override
    public void response(String sessionId, String data) {
        publishToQueue(StreamCommandIdentity.RESPONSE, sessionId, null, data, null);
    }

    /**
     * Push data to channels subscribed to the topic.
     */
    @Override
    public void push(String topic, String data) {
        publishToQueue(StreamCommandIdentity.PUSH, null, topic, data, null);
    }

    /**
     * All active channels flush regularly.
     */
    @Override
    public void flush() {
        publishToQueue(StreamCommandIdentity.FLUSH, null, null, null, null);
    }

    /**
     * Remove a channel.
     */
    @Override
    public void remove(String sessionId) {
        publishToQueue(StreamCommandIdentity.REMOVE, sessionId, null, null, null);
    }

    private void publishToQueue(StreamCommandIdentity commandIdentity, String sessionId, String topic, String data,
        Channel channel) {
        final EventTranslator<StreamWorkerEvent> translator = (event, sequence) -> {
            event.setIdentity(commandIdentity);
            event.setSessionId(sessionId);
            event.setTopic(topic);
            event.setData(data);
            event.setChannel(channel);
        };
        /**
         * FIXME: If the queue is full, publishEvent will be blocking, the system is blocked.
         */
        this.workerQueue.publishEvent(translator);
    }

    /**
     * StreamWorkerEvent handler for Disruptor
     */
    private class StreamWorkerHandler implements EventHandler<StreamWorkerEvent> {

        @Override
        public void onEvent(final StreamWorkerEvent event, final long sequence, final boolean endOfBatch)
            throws Exception {
            switch (event.getIdentity()) {
                case ADD:
                    addHandler(event.getSessionId(), event.getChannel());
                    break;
                case PING:
                    pingHandler();
                    break;
                case PONG:
                    pongHandler(event.getSessionId());
                    break;
                case SUBSCRIBE:
                    subscribeHandler(event.getSessionId(), event.getTopic());
                    break;
                case UNSUBSCRIBE:
                    unsubscribeHandler(event.getSessionId(), event.getTopic());
                    break;
                case RESPONSE:
                    responseHandler(event.getSessionId(), event.getData());
                    break;
                case PUSH:
                    pushHandler(event.getTopic(), event.getData());
                    break;
                case FLUSH:
                    flushHandler();
                    break;
                case REMOVE:
                    removeHandler(event.getSessionId());
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * add handler.
     */
    private void addHandler(String sessionId, Channel channel) {
        SessionContext session = sessionMap.get(sessionId);
        if (session != null) {
            log.warn("duplicate websocket session id: {}", sessionId);
        } else {
            session = new SessionContext();
            sessionMap.put(sessionId, session);
        }
        session.setChannel(channel);
        session.setSubscribeTopics(new HashSet<>());
        session.setMissingPongCount(0);
        session.setShouldFlush(false);
    }

    /**
     * ping handler. Send ping to all active channels regularly; If the count of pongs sent by the client is not
     * received exceeds the threshold, close the channel.
     */
    private void pingHandler() {
        for (SessionContext session : sessionMap.values()) {
            session.setMissingPongCount(session.getMissingPongCount() + 1);
            if (session.getMissingPongCount() > MAX_MISSING_PONGS) {
                session.getChannel().close();
            } else {
                session.getChannel().write(new TextWebSocketFrame(Constants.PING));
                session.setShouldFlush(true);
            }
        }
    }

    /**
     * pong handler. Receive pong from the channel, proving that the channel is active.
     */
    private void pongHandler(String sessionId) {
        SessionContext session = sessionMap.get(sessionId);
        if (session != null) {
            session.setMissingPongCount(0);
        }
    }

    /**
     * subscribe handler.
     */
    private void subscribeHandler(String sessionId, String topic) {
        SessionContext session = sessionMap.get(sessionId);
        if (session == null) {
            log.error("the websocket session id({}) does not exist", sessionId);
        } else {
            if (StringUtils.isNotEmpty(topic)) {
                Set<String> subscribeTopics = session.getSubscribeTopics();
                subscribeTopics.add(topic);

                Set<String> sessionSet = topicMap.get(topic);
                if (sessionSet == null) {
                    sessionSet = new HashSet<>();
                    topicMap.put(topic, sessionSet);
                }
                sessionSet.add(sessionId);
            }
        }
    }

    /**
     * unsubscribe handler.
     */
    private void unsubscribeHandler(String sessionId, String topic) {
        SessionContext session = sessionMap.get(sessionId);
        if (session == null) {
            log.error("the websocket session id({}) does not exist", sessionId);
        } else {
            if (StringUtils.isNotEmpty(topic)) {
                Set<String> subscribeTopics = session.getSubscribeTopics();
                subscribeTopics.remove(topic);

                Set<String> sessionSet = topicMap.get(topic);
                if (sessionSet != null) {
                    sessionSet.remove(sessionId);
                    if (sessionSet.size() == 0) {
                        topicMap.remove(topic);
                    }
                }
            }
        }
    }

    /**
     * response handler.
     */
    private void responseHandler(String sessionId, String data) {
        SessionContext session = sessionMap.get(sessionId);
        if (session == null) {
            log.error("the websocket session id({}) does not exist", sessionId);
        } else {
            session.getChannel().write(new TextWebSocketFrame(data));
            session.setShouldFlush(true);
        }
    }

    /**
     * push handler.
     */
    private void pushHandler(String topic, String data) {
        Set<String> sessionSet = topicMap.get(topic);
        if (!CollectionUtils.isEmpty(sessionSet)) {
            ByteBufHolder byteBufHolder = new TextWebSocketFrame(data);
            for (String sessionId : sessionSet) {
                SessionContext session = sessionMap.get(sessionId);
                if (session == null) {
                    log.error("the websocket session id({}) does not exist", sessionId);
                } else {
                    session.getChannel().write(byteBufHolder.retainedDuplicate());
                    session.setShouldFlush(true);
                }
            }
        }
    }

    /**
     * flush handler.
     */
    private void flushHandler() {
        for (SessionContext session : sessionMap.values()) {
            if (session.isShouldFlush()) {
                session.getChannel().flush();
                session.setShouldFlush(false);
            }
        }
    }

    /**
     * remove handler.
     */
    private void removeHandler(String sessionId) {
        SessionContext session = sessionMap.get(sessionId);
        if (session == null) {
            log.error("the websocket session id({}) does not exist", sessionId);
        } else {
            Set<String> subscribeTopics = session.getSubscribeTopics();
            for (String topic : subscribeTopics) {
                Set<String> sessionSet = topicMap.get(topic);
                if (sessionSet != null) {
                    sessionSet.remove(sessionId);
                    if (sessionSet.size() == 0) {
                        topicMap.remove(topic);
                    }
                }
            }
            subscribeTopics.clear();
            sessionMap.remove(sessionId);
        }
    }

    /**
     * StreamWorkerEvent factory for Disruptor
     */
    private static class StreamWorkerFactory implements EventFactory<StreamWorkerEvent> {

        @Override
        public StreamWorkerEvent newInstance() {
            return new StreamWorkerEvent();
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private static class SessionContext {

        /**
         * channel
         */
        private Channel channel;

        /**
         * subscribe topics
         */
        private Set<String> subscribeTopics;

        /**
         * missing pong count
         */
        private Integer missingPongCount;

        /**
         * whether to flush
         */
        private boolean shouldFlush;
    }
}
