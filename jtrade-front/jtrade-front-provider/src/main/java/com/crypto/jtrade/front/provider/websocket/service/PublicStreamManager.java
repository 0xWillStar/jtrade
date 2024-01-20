package com.crypto.jtrade.front.provider.websocket.service;

/**
 * public stream manager
 *
 * @author 0xWill
 **/
public interface PublicStreamManager extends StreamManager {

    /**
     * push data to websocket client
     */
    void push(String topic, String data);

}
