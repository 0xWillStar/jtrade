package com.crypto.jtrade.core.provider.service.trade;

import com.crypto.jtrade.core.provider.model.queue.CommandEvent;

/**
 * trade log
 *
 * @author 0xWill
 **/
public interface TradeLog {

    /**
     * publish log
     */
    void publishLog(CommandEvent commandEvent);

}
