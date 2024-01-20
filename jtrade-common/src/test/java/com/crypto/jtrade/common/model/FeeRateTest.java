package com.crypto.jtrade.common.model;

import com.crypto.jtrade.common.util.myserialize.MySerializeUtils;
import com.crypto.jtrade.common.util.myserialize.MySqlUtils;
import org.junit.Test;

public class FeeRateTest {
    
    @Test
    public void toStringCode() {
        System.out.println(MySerializeUtils.toStringCode(FeeRate.class));
    }
    
    @Test
    public void toObjectCode() {
        System.out.println(MySerializeUtils.toObjectCode(FeeRate.class));
    }
    
    @Test
    public void toJSONCode() {
        System.out.println(MySerializeUtils.toJSONCode(FeeRate.class));
    }
    
    @Test
    public void getInsertSql() {
        System.out.println(MySqlUtils.getInsertSql(FeeRate.class));
    }
    
    @Test
    public void getUpdateSql() {
        System.out.println(MySqlUtils.getUpdateSql(FeeRate.class));
    }
    
    @Test
    public void getDeleteSql() {
        System.out.println(MySqlUtils.getDeleteSql(FeeRate.class));
    }
}