package com.crypto.jtrade.common.model;

import org.junit.Test;

import com.crypto.jtrade.common.util.myserialize.MySerializeUtils;

public class IndexPriceTest {

    @Test
    public void toJSONCode() {
        System.out.println(MySerializeUtils.toJSONCode(IndexPrice.class));
    }

}
