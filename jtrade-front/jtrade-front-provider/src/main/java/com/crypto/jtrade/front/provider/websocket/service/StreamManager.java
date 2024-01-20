package com.crypto.jtrade.front.provider.websocket.service;

import io.netty.channel.Channel;

/**
 * stream manager
 *
 * @author 0xWill
 **/
public interface StreamManager {

    /**
     * add new channel
     */
    void addChannel(String sessionId, Channel channel);

    /**
     * remove channel
     */
    void removeChannel(String sessionId);

    /**
     * websocket client request
     */
    void request(String sessionId, Channel channel, String message);

}
