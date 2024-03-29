package com.crypto.jtrade.core.provider.service.trade;

import com.crypto.jtrade.core.provider.model.queue.CommandEvent;

/**
 * Trade service
 *
 * @author 0xWill
 **/
public interface TradeService {

    /**
     * publish command
     */
    void publishCommand(CommandEvent commandEvent);

}
