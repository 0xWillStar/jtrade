package com.crypto.jtrade.core.provider.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.model.Depth;
import com.crypto.jtrade.common.model.Kline;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.SymbolIndicator;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.model.Ticker;
import com.crypto.jtrade.common.util.ResponseHelper;
import com.crypto.jtrade.core.api.QueryApi;
import com.crypto.jtrade.core.provider.service.query.QueryService;

/**
 * query api controller
 *
 * @author 0xWill
 **/
@RestController
public class QueryController implements QueryApi {

    @Autowired
    private QueryService queryService;

    @Override
    public BaseResponse<List<SymbolInfo>> getSymbols() {
        return ResponseHelper.success(queryService.getSymbols());
    }

    @Override
    public BaseResponse<SymbolIndicator> getSymbolIndicator(String symbol) {
        return ResponseHelper.success(queryService.getSymbolIndicator(symbol));
    }

    @Override
    public BaseResponse<Depth> getDepth(String symbol) {
        return ResponseHelper.success(queryService.getDepth(symbol));
    }

    @Override
    public BaseResponse<Ticker> getTicker(String symbol) {
        return ResponseHelper.success(queryService.getTicker(symbol));
    }

    @Override
    public BaseResponse<Kline> getKline(String symbol, String period) {
        return ResponseHelper.success(queryService.getKline(symbol, period));
    }

    @Override
    public BaseResponse<List<Order>> getOpenOrders(String clientId) {
        return ResponseHelper.success(queryService.getOpenOrders(clientId));
    }

    @Override
    public BaseResponse<List<Position>> getPositions(String clientId) {
        return ResponseHelper.success(queryService.getPositions(clientId));
    }

    @Override
    public BaseResponse<List<AssetBalance>> getBalances(String clientId) {
        return ResponseHelper.success(queryService.getBalances(clientId));
    }
}
