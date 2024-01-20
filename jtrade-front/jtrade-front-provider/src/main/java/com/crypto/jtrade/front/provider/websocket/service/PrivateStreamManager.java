package com.crypto.jtrade.front.provider.websocket.service;

/**
 * private stream manager
 *
 * @author 0xWill
 **/
public interface PrivateStreamManager extends StreamManager {

    /**
     * push data to websocket client
     */
    void push(String clientId, String topic, String data);

}
