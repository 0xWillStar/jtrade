package com.crypto.jtrade.common.model;

import com.crypto.jtrade.common.util.myserialize.MySerializeUtils;
import com.crypto.jtrade.common.util.myserialize.MySqlUtils;
import org.junit.Test;

public class TradeTest {
    
    @Test
    public void toStringCode() {
        System.out.println(MySerializeUtils.toStringCode(Trade.class));
    }
    
    @Test
    public void toObjectCode() {
        System.out.println(MySerializeUtils.toObjectCode(Trade.class));
    }
    
    @Test
    public void toJSONCode() {
        System.out.println(MySerializeUtils.toJSONCode(Trade.class));
    }
    
    @Test
    public void getInsertSql() {
        System.out.println(MySqlUtils.getInsertSql(Trade.class));
    }
    
    @Test
    public void getUpdateSql() {
        System.out.println(MySqlUtils.getUpdateSql(Trade.class));
    }
    
    @Test
    public void getDeleteSql() {
        System.out.println(MySqlUtils.getDeleteSql(Trade.class));
    }
}