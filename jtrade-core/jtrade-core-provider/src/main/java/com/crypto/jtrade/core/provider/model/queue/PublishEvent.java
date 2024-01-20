package com.crypto.jtrade.core.provider.model.queue;

import java.io.Serializable;

import com.crypto.jtrade.common.constants.CommandIdentity;

import lombok.Data;

/**
 * jtrade publish object
 *
 * @author 0xWill
 **/
@Data
public class PublishEvent<T> implements Serializable {

    private static final long serialVersionUID = 6078837528571205783L;

    private CommandIdentity identity;

    private T data;

}
