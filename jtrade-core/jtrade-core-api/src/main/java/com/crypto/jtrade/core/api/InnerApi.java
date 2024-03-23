package com.crypto.jtrade.core.api;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.model.ClientInfo;
import com.crypto.jtrade.common.model.SymbolInfo;

/**
 * define inner api
 *
 * @author 0xWill
 **/
public interface InnerApi {

    @GetMapping(value = "/v1/inner/parameters")
    BaseResponse<Map<SystemParameter, String>> getSystemParametersFromRedis();

    @GetMapping(value = "/v1/inner/symbols")
    BaseResponse<List<SymbolInfo>> getSymbolsFromRedis(@RequestParam(required = false) String symbol);

    @GetMapping(value = "/v1/inner/client")
    BaseResponse<ClientInfo> getClientInfoFromRedis(@RequestParam String id);

    @GetMapping(value = "/v1/inner/orderClientIds")
    BaseResponse<Set<String>> getOrderClientIdsFromRedis();

    @GetMapping(value = "/v1/inner/positionClientIds")
    BaseResponse<Set<String>> getPositionClientIdsFromRedis();

    @GetMapping(value = "/v1/inner/cancelOrder")
    BaseResponse cancelClientOrder(@RequestParam String clientId, @RequestParam(required = false) Long orderId);

    @GetMapping(value = "/v1/inner/closePosition")
    BaseResponse closeClientPosition(@RequestParam String clientId, @RequestParam(required = false) String symbol);

    @GetMapping(value = "/v1/inner/exportOrderBook")
    BaseResponse exportOrderBook(@RequestParam String symbol);

    @GetMapping(value = "/v1/inner/exportOrders")
    BaseResponse exportOrders(@RequestParam(required = false) String clientId,
        @RequestParam(required = false) String symbol);

    @GetMapping(value = "/v1/inner/exportPositions")
    BaseResponse exportPositions(@RequestParam(required = false) String clientId,
        @RequestParam(required = false) String symbol);

}
