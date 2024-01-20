package com.crypto.jtrade.common.model;

import com.crypto.jtrade.common.util.myserialize.MySerializeUtils;
import com.crypto.jtrade.common.util.myserialize.MySqlUtils;
import org.junit.Test;

public class AssetBalanceTest {
    
    @Test
    public void toStringCode() {
        System.out.println(MySerializeUtils.toStringCode(AssetBalance.class));
    }
    
    @Test
    public void toObjectCode() {
        System.out.println(MySerializeUtils.toObjectCode(AssetBalance.class));
    }
    
    @Test
    public void toJSONCode() {
        System.out.println(MySerializeUtils.toJSONCode(AssetBalance.class));
    }
    
    @Test
    public void getInsertSql() {
        System.out.println(MySqlUtils.getInsertSql(AssetBalance.class));
    }
    
    @Test
    public void getUpdateSql() {
        System.out.println(MySqlUtils.getUpdateSql(AssetBalance.class));
    }
    
    @Test
    public void getDeleteSql() {
        System.out.println(MySqlUtils.getDeleteSql(AssetBalance.class));
    }
}