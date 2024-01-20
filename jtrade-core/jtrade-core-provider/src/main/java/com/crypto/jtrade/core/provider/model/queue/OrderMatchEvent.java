package com.crypto.jtrade.core.provider.model.queue;

import java.io.Serializable;

import com.crypto.jtrade.common.constants.CommandIdentity;

import lombok.Data;

/**
 * Order for match
 *
 * @author 0xWill
 **/
@Data
public class OrderMatchEvent implements Serializable {

    private static final long serialVersionUID = 5857465897378472490L;

    private CommandIdentity identity;

    private Object order;
}
