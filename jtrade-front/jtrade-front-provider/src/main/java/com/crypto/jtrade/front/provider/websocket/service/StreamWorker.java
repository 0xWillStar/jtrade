package com.crypto.jtrade.front.provider.websocket.service;

import com.crypto.jtrade.common.util.NamedThreadFactory;

import io.netty.channel.Channel;

/**
 * websocket stream worker service
 *
 * @author 0xWill
 **/
public interface StreamWorker {

    /**
     * init StreamWorker
     */
    void init(int workerId, NamedThreadFactory threadFactory);

    /**
     * get worker id
     */
    int getWorkerId();

    /**
     * A new channel is connected.
     */
    void add(String sessionId, Channel channel);

    /**
     * Send ping to all active channels regularly.
     */
    void ping();

    /**
     * Received pong from a channel.
     */
    void pong(String sessionId);

    /**
     * A channel subscribes topics.
     */
    void subscribe(String sessionId, String topic);

    /**
     * A channel unsubscribes topics.
     */
    void unsubscribe(String sessionId, String topic);

    /**
     * Response to subscribe or unsubscribe
     */
    void response(String sessionId, String data);

    /**
     * Push data to channels subscribed to the topic.
     */
    void push(String topic, String data);

    /**
     * All active channels flush regularly.
     */
    void flush();

    /**
     * Remove a channel.
     */
    void remove(String sessionId);

}
