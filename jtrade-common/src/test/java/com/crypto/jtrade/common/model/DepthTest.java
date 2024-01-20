package com.crypto.jtrade.common.model;

import com.crypto.jtrade.common.util.myserialize.MySerializeUtils;
import org.junit.Test;

public class DepthTest {
    
    @Test
    public void toJSONCode() {
        System.out.println(MySerializeUtils.toJSONCode(Depth.class));
    }
}