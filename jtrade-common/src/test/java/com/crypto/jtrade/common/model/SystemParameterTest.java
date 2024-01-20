package com.crypto.jtrade.common.model;

import com.crypto.jtrade.common.util.myserialize.MySerializeUtils;
import com.crypto.jtrade.common.util.myserialize.MySqlUtils;
import org.junit.Test;

public class SystemParameterTest {
    
    @Test
    public void toStringCode() {
        System.out.println(MySerializeUtils.toStringCode(SystemParameter.class));
    }
    
    @Test
    public void toObjectCode() {
        System.out.println(MySerializeUtils.toObjectCode(SystemParameter.class));
    }
    
    @Test
    public void toJSONCode() {
        System.out.println(MySerializeUtils.toJSONCode(SystemParameter.class));
    }
    
    @Test
    public void getInsertSql() {
        System.out.println(MySqlUtils.getInsertSql(SystemParameter.class));
    }
    
    @Test
    public void getUpdateSql() {
        System.out.println(MySqlUtils.getUpdateSql(SystemParameter.class));
    }
    
    @Test
    public void getDeleteSql() {
        System.out.println(MySqlUtils.getDeleteSql(SystemParameter.class));
    }
}