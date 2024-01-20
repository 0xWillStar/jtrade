package com.crypto.jtrade.common.model;

import com.crypto.jtrade.common.util.myserialize.MySerializeUtils;
import org.junit.Test;

public class KlineTest {
    
    @Test
    public void toStringCode() {
        System.out.println(MySerializeUtils.toStringCode(Kline.class));
    }
    
    @Test
    public void toObjectCode() {
        System.out.println(MySerializeUtils.toObjectCode(Kline.class));
    }
    
    @Test
    public void toJSONCode() {
        System.out.println(MySerializeUtils.toJSONCode(Kline.class));
    }
}