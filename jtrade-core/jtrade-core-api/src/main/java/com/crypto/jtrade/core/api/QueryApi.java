package com.crypto.jtrade.core.api;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.model.Depth;
import com.crypto.jtrade.common.model.Kline;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.SymbolIndicator;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.model.Ticker;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

/**
 * define query api
 *
 * @author 0xWill
 **/
@Headers("Content-Type: application/json")
public interface QueryApi {

    @GetMapping(value = "/v1/query/symbols")
    @RequestLine("GET /v1/query/symbols")
    BaseResponse<List<SymbolInfo>> getSymbols();

    @GetMapping(value = "/v1/query/symbolIndicator")
    @RequestLine("GET /v1/query/symbolIndicator?symbol={symbol}")
    BaseResponse<SymbolIndicator> getSymbolIndicator(@Param("symbol") @RequestParam String symbol);

    @GetMapping(value = "/v1/query/depth")
    @RequestLine("GET /v1/query/depth?symbol={symbol}")
    BaseResponse<Depth> getDepth(@Param("symbol") @RequestParam String symbol);

    @GetMapping(value = "/v1/query/ticker")
    @RequestLine("GET /v1/query/ticker?symbol={symbol}")
    BaseResponse<Ticker> getTicker(@Param("symbol") @RequestParam String symbol);

    @GetMapping(value = "/v1/query/kline")
    @RequestLine("GET /v1/query/kline?symbol={symbol}&period={period}")
    BaseResponse<Kline> getKline(@Param("symbol") @RequestParam String symbol,
        @Param("period") @RequestParam String period);

    @GetMapping(value = "/v1/query/openOrders")
    @RequestLine("GET /v1/query/openOrders?clientId={clientId}")
    BaseResponse<List<Order>> getOpenOrders(@Param("clientId") @RequestParam String clientId);

    @GetMapping(value = "/v1/query/positions")
    @RequestLine("GET /v1/query/positions?clientId={clientId}")
    BaseResponse<List<Position>> getPositions(@Param("clientId") @RequestParam String clientId);

    @GetMapping(value = "/v1/query/balances")
    @RequestLine("GET /v1/query/balances?clientId={clientId}")
    BaseResponse<List<AssetBalance>> getBalances(@Param("clientId") @RequestParam String clientId);

}
