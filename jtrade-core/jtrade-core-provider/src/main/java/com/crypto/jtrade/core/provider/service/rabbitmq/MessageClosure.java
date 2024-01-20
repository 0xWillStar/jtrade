package com.crypto.jtrade.core.provider.service.rabbitmq;

import com.crypto.jtrade.common.constants.CommandIdentity;

/**
 * callback closure
 *
 * @author 0xWill
 **/
public interface MessageClosure {

    /**
     * called when sending data
     */
    void addToBatch(String data, boolean trySend);

    /**
     * publish data to queue
     */
    void publishToQueue(CommandIdentity identity, Object data);

}
