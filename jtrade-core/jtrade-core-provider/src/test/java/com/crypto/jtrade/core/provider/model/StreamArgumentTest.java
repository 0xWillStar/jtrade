package com.crypto.jtrade.core.provider.model;

import org.junit.Test;

import com.crypto.jtrade.common.model.StreamArgument;
import com.crypto.jtrade.common.util.myserialize.MySerializeUtils;

public class StreamArgumentTest {

    @Test
    public void toJSONString() {
        System.out.println(MySerializeUtils.toJSONCode(StreamArgument.class));
    }
}