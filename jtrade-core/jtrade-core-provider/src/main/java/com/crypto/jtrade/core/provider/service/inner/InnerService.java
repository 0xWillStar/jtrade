package com.crypto.jtrade.core.provider.service.inner;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.model.ClientInfo;
import com.crypto.jtrade.common.model.SymbolInfo;

/**
 * inner service
 *
 * @author 0xWill
 **/
public interface InnerService {

    Map<SystemParameter, String> getSystemParametersFromRedis();

    List<SymbolInfo> getSymbolsFromRedis(String symbol);

    ClientInfo getClientInfoFromRedis(String clientId);

    Set<String> getOrderClientIdsFromRedis();

    Set<String> getPositionClientIdsFromRedis();

    void cancelClientOrder(String clientId, Long orderId);

    void closeClientPosition(String clientId, String symbol);

    void exportOrderBook(String symbol);

    void exportOrders(String clientId, String symbol);

    void exportPositions(String clientId, String symbol);

}
