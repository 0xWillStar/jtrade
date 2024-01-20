package com.crypto.jtrade.front.provider.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.crypto.jtrade.common.model.Trade;

/**
 * trade mapper
 *
 * @author 0xWill
 **/
@Repository
public interface TradeMapper {

    List<Trade> getTradeList(String symbol, Long startTime, Integer limit);

    List<Trade> getTradeListByClient(String clientId, Long startTime, Integer limit);

    List<Trade> getHistoryTrades(String clientId, String symbol, Long fromId, Long startTime, Long endTime, Long limit);

}
