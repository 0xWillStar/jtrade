package com.crypto.jtrade.sinkdb.service.worker.operation.impl;

import org.junit.Before;
import org.junit.Test;

public class SymbolInfoOperationTest {

    private SymbolInfoOperation symbolInfoOperation;

    private String data;

    @Before
    public void init() {
        symbolInfoOperation = new SymbolInfoOperation();
        data =
            ",PERPETUAL,ETH-USDC,,NET,,,1,USDT,USDC,,,,,,,0.01,0.001,false,,,1669702117,,CONTINUOUS,100,20,0.03,true,2,2,3,5000";
    }

    @Test
    public void insert() {
        System.out.println(symbolInfoOperation.insert(data));
    }

    @Test
    public void update() {
        System.out.println(symbolInfoOperation.update(data));
    }

    @Test
    public void delete() {
        System.out.println(symbolInfoOperation.delete(data));
    }
}