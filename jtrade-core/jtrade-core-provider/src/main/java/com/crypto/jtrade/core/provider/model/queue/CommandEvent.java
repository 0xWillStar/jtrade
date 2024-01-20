package com.crypto.jtrade.core.provider.model.queue;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;

import com.alibaba.fastjson.annotation.JSONField;
import com.crypto.jtrade.common.constants.CommandIdentity;
import com.crypto.jtrade.common.model.BaseResponse;

import lombok.Data;

/**
 * jtrade command
 *
 * @author 0xWill
 **/
@Data
public class CommandEvent<T> implements Serializable {

    private static final long serialVersionUID = -8252879650425926665L;

    private Long requestId;

    private CommandIdentity identity;

    private T request;

    @JSONField(serialize = false)
    private CompletableFuture<BaseResponse> future;
}
