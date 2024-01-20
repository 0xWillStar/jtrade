package com.crypto.jtrade.core.provider.service.stop;

import com.crypto.jtrade.core.provider.model.queue.TriggerPriceEvent;

/**
 * stop order service
 *
 * @author 0xWill
 **/
public interface StopService {

    /**
     * set the trigger price
     */
    void setTriggerPrice(TriggerPriceEvent triggerPriceEvent);

}
