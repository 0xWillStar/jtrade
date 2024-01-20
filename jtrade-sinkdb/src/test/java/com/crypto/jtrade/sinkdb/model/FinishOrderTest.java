package com.crypto.jtrade.sinkdb.model;

import org.junit.Test;

import com.crypto.jtrade.common.util.myserialize.MySqlUtils;

public class FinishOrderTest {

    @Test
    public void getInsertSql() {
        System.out.println(MySqlUtils.getInsertSql(FinishOrder.class));
    }

    @Test
    public void getUpdateSql() {
        System.out.println(MySqlUtils.getUpdateSql(FinishOrder.class));
    }

    @Test
    public void getDeleteSql() {
        System.out.println(MySqlUtils.getDeleteSql(FinishOrder.class));
    }
}