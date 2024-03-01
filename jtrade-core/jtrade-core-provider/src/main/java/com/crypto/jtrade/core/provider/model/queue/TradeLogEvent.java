package com.crypto.jtrade.core.provider.model.queue;

import java.io.Serializable;

import lombok.Data;

/**
 * trade log event
 *
 * @author 0xWill
 **/
@Data
public class TradeLogEvent<T> implements Serializable {

    private static final long serialVersionUID = 5604468696520408256L;

    private boolean timeToForce;

    private CommandEvent<T> commandEvent;

}
