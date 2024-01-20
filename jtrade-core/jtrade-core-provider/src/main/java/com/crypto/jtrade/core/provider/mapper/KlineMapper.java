package com.crypto.jtrade.core.provider.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.crypto.jtrade.common.model.Kline;

/**
 * kline mapper
 *
 * @author 0xWill
 **/
@Repository
public interface KlineMapper {

    Long getFirstKlineTime(String symbol, String period, String tableName);

    Long getLastKlineTime(String symbol, String period, String tableName);

    Kline getKline(String symbol, String period, Long beginTime, String tableName);

    Kline statKline(String symbol, String period, Long firstTime, Long lastTime, String tableName);

    void addKline(Kline kline);

    List<Kline> getKlineList(String symbol, String period, Long beginTime, Long endTime, String tableName);

}
