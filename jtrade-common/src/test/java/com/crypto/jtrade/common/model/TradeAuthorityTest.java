package com.crypto.jtrade.common.model;

import com.crypto.jtrade.common.util.myserialize.MySerializeUtils;
import com.crypto.jtrade.common.util.myserialize.MySqlUtils;
import org.junit.Test;

public class TradeAuthorityTest {
    
    @Test
    public void toStringCode() {
        System.out.println(MySerializeUtils.toStringCode(TradeAuthority.class));
    }
    
    @Test
    public void toObjectCode() {
        System.out.println(MySerializeUtils.toObjectCode(TradeAuthority.class));
    }
    
    @Test
    public void toJSONCode() {
        System.out.println(MySerializeUtils.toJSONCode(TradeAuthority.class));
    }
    
    @Test
    public void getInsertSql() {
        System.out.println(MySqlUtils.getInsertSql(TradeAuthority.class));
    }
    
    @Test
    public void getUpdateSql() {
        System.out.println(MySqlUtils.getUpdateSql(TradeAuthority.class));
    }
    
    @Test
    public void getDeleteSql() {
        System.out.println(MySqlUtils.getDeleteSql(TradeAuthority.class));
    }
}