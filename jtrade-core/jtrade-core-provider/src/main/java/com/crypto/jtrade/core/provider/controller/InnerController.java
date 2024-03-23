package com.crypto.jtrade.core.provider.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.model.ClientInfo;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.util.ResponseHelper;
import com.crypto.jtrade.core.api.InnerApi;
import com.crypto.jtrade.core.provider.service.inner.InnerService;

/**
 * inner api controller
 *
 * @author 0xWill
 **/
@RestController
public class InnerController implements InnerApi {

    @Autowired
    private InnerService innerService;

    @Override
    public BaseResponse<Map<SystemParameter, String>> getSystemParametersFromRedis() {
        return ResponseHelper.success(innerService.getSystemParametersFromRedis());
    }

    @Override
    public BaseResponse<List<SymbolInfo>> getSymbolsFromRedis(String symbol) {
        return ResponseHelper.success(innerService.getSymbolsFromRedis(symbol));
    }

    @Override
    public BaseResponse<ClientInfo> getClientInfoFromRedis(String id) {
        return ResponseHelper.success(innerService.getClientInfoFromRedis(id));
    }

    @Override
    public BaseResponse<Set<String>> getOrderClientIdsFromRedis() {
        return ResponseHelper.success(innerService.getOrderClientIdsFromRedis());
    }

    @Override
    public BaseResponse<Set<String>> getPositionClientIdsFromRedis() {
        return ResponseHelper.success(innerService.getPositionClientIdsFromRedis());
    }

    @Override
    public BaseResponse cancelClientOrder(String clientId, Long orderId) {
        innerService.cancelClientOrder(clientId, orderId);
        return ResponseHelper.success();
    }

    @Override
    public BaseResponse closeClientPosition(String clientId, String symbol) {
        innerService.closeClientPosition(clientId, symbol);
        return ResponseHelper.success();
    }

    @Override
    public BaseResponse exportOrderBook(String symbol) {
        innerService.exportOrderBook(symbol);
        return ResponseHelper.success();
    }

    @Override
    public BaseResponse exportOrders(String clientId, String symbol) {
        innerService.exportOrders(clientId, symbol);
        return ResponseHelper.success();
    }

    @Override
    public BaseResponse exportPositions(String clientId, String symbol) {
        innerService.exportPositions(clientId, symbol);
        return ResponseHelper.success();
    }

}
