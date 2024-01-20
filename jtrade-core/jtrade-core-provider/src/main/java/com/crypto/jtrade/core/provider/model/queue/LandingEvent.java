package com.crypto.jtrade.core.provider.model.queue;

import java.io.Serializable;

import com.crypto.jtrade.common.constants.CommandIdentity;

import lombok.Data;

/**
 * jtrade landing
 *
 * @author 0xWill
 **/
@Data
public class LandingEvent<T> implements Serializable {

    private static final long serialVersionUID = -8092051646543489741L;

    private CommandIdentity identity;

    private T data;

}
