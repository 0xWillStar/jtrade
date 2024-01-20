package com.crypto.jtrade.front.provider.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.crypto.jtrade.common.model.Bill;

/**
 * bill mapper
 *
 * @author 0xWill
 **/
@Repository
public interface BillMapper {

    List<Bill> getBillList(String clientId, Long startTime, Integer limit);

    List<Bill> getHistoryBills(String clientId, String symbol, Long startTime, Long endTime, Long limit);

}
