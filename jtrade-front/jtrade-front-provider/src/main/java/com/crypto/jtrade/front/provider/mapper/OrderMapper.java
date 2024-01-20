package com.crypto.jtrade.front.provider.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.crypto.jtrade.common.model.Order;

/**
 * order mapper
 *
 * @author 0xWill
 **/
@Repository
public interface OrderMapper {

    List<Order> getFinishOrderList(String clientId, Long startTime, Integer limit);

    Order getFinishOrder(String clientId, String symbol, String clientOrderId, Long orderId);

    List<Order> getHistoryOrders(String clientId, String symbol, Long orderId, Long startTime, Long endTime,
        Long limit);

}
