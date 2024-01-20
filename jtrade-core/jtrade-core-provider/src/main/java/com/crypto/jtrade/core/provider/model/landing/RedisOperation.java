package com.crypto.jtrade.core.provider.model.landing;

import com.crypto.jtrade.common.constants.RedisOp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * redis operation
 *
 * @author 0xWill
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisOperation {

    private String redisKey;

    private String hashKey;

    private String value;

    private Boolean deleted;

    private RedisOp redisOp;

}
