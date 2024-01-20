package com.crypto.jtrade.front.provider.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.crypto.jtrade.common.constants.MarginType;
import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.model.Bill;
import com.crypto.jtrade.common.model.ClientSetting;
import com.crypto.jtrade.common.model.FeeRate;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.model.Trade;
import com.crypto.jtrade.core.api.TradeApi;
import com.crypto.jtrade.core.api.model.AdjustPositionMarginRequest;
import com.crypto.jtrade.core.api.model.CancelOrderRequest;
import com.crypto.jtrade.core.api.model.ClientSettingRequest;
import com.crypto.jtrade.core.api.model.PlaceOrderRequest;
import com.crypto.jtrade.core.api.model.PlaceOrderResponse;
import com.crypto.jtrade.core.api.model.WithdrawRequest;
import com.crypto.jtrade.front.api.model.LoginRequest;
import com.crypto.jtrade.front.api.model.LoginResponse;
import com.crypto.jtrade.front.provider.cache.PrivateCache;
import com.crypto.jtrade.front.provider.cache.PublicCache;
import com.crypto.jtrade.front.provider.cache.RedisService;
import com.crypto.jtrade.front.provider.config.FrontConfig;
import com.crypto.jtrade.front.provider.mapper.BillMapper;
import com.crypto.jtrade.front.provider.mapper.OrderMapper;
import com.crypto.jtrade.front.provider.mapper.TradeMapper;
import com.crypto.jtrade.front.provider.model.ApiKeyInfo;
import com.crypto.jtrade.front.provider.service.TradeService;
import com.crypto.jtrade.front.provider.util.SignatureUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * front trade service
 *
 * @author 0xWill
 */
@Service
@Slf4j
public class TradeServiceImpl implements TradeService {

    @Autowired
    private FrontConfig frontConfig;

    @Autowired
    private RedisService redisService;

    @Autowired
    private PublicCache publicCache;

    @Autowired
    private PrivateCache privateCache;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TradeMapper tradeMapper;

    @Autowired
    private BillMapper billMapper;

    @Autowired
    private TradeApi tradeApi;

    @Override
    public LoginResponse login(String address, String timestamp, String signature, String host, LoginRequest request) {
        if (StringUtils.isBlank(address)) {
            throw new TradeException(TradeError.ADDRESS_INVALID);
        }
        try {
            if (Math.abs(System.currentTimeMillis() - Long.parseLong(timestamp)) > 60_000L) {
                throw new TradeException(TradeError.TIMESTAMP_INVALID);
            }
        } catch (Exception e) {
            throw new TradeException(TradeError.TIMESTAMP_INVALID);
        }

        try {
            boolean passed = SignatureUtil.checkLogin(signature, address, timestamp, host, frontConfig.getName());
            if (!passed) {
                throw new TradeException(TradeError.SIGNATURE_INVALID);
            }
        } catch (Exception e) {
            throw new TradeException(TradeError.SIGNATURE_INVALID);
        }
        final String apiKey = UUID.randomUUID().toString();
        final String apiSecret = UUID.randomUUID().toString();
        redisService.setApiKeyInfo(ApiKeyInfo.builder().apiKey(apiKey).apiSecret(apiSecret).clientId(address).build());
        return LoginResponse.builder().apiKey(apiKey).apiSecret(apiSecret).build();
    }

    @Override
    public BaseResponse setClientSetting(ClientSettingRequest request) {
        return tradeApi.setClientSetting(request);
    }

    @Override
    public BaseResponse withdraw(WithdrawRequest request) {
        return tradeApi.withdraw(request);
    }

    @Override
    public BaseResponse<PlaceOrderResponse> placeOrder(PlaceOrderRequest request) {
        return tradeApi.placeOrder(request);
    }

    @Override
    public BaseResponse<PlaceOrderResponse> placeOTOOrder(List<PlaceOrderRequest> request) {
        return tradeApi.placeOTOOrder(request);
    }

    @Override
    public BaseResponse cancelOrder(CancelOrderRequest request) {
        return tradeApi.cancelOrder(request);
    }

    @Override
    public BaseResponse adjustPositionMargin(AdjustPositionMarginRequest request) {
        return tradeApi.adjustPositionMargin(request);
    }

    @Override
    public List<Order> getOpenOrders(String clientId, String symbol) {
        Map<Long, Order> orderMap = privateCache.getOpenOrders(clientId);
        List<Order> orderList = new ArrayList<>(orderMap.values());
        if (symbol != null) {
            orderList =
                orderList.stream().filter(order -> order.getSymbol().equals(symbol)).collect(Collectors.toList());
        }
        return orderList;
    }

    @Override
    public List<Position> getPositions(String clientId, String symbol) {
        Map<String, Position> positionMap = privateCache.getPositions(clientId);
        List<Position> positionList = new ArrayList<>(positionMap.values());
        if (symbol != null) {
            positionList = positionList.stream().filter(position -> position.getSymbol().equals(symbol))
                .collect(Collectors.toList());
        }
        return positionList;
    }

    @Override
    public List<AssetBalance> getBalances(String clientId, String asset) {
        Map<String, AssetBalance> balanceMap = privateCache.getBalances(clientId);
        List<AssetBalance> balanceList = new ArrayList<>(balanceMap.values());
        if (asset != null) {
            balanceList =
                balanceList.stream().filter(balance -> balance.getAsset().equals(asset)).collect(Collectors.toList());
        }
        return balanceList;
    }

    @Override
    public Order getOrder(String clientId, String symbol, String clientOrderId, Long orderId) {
        Map<Long, Order> orderMap = privateCache.getOpenOrders(clientId);
        Order order = filterOrder(orderMap.values(), symbol, clientOrderId, orderId);
        if (order == null) {
            Collection<Order> orders = privateCache.getFinishOrders(clientId);
            order = filterOrder(orders, symbol, clientOrderId, orderId);
        }
        if (order == null) {
            order = orderMapper.getFinishOrder(clientId, symbol, clientOrderId, orderId);
        }
        return order;
    }

    @Override
    public List<Order> getHistoryOrders(String clientId, String symbol, Long orderId, Long startTime, Long endTime,
        Long limit) {
        List<Order> orderList;
        if (symbol == null && orderId == null && startTime == null && endTime == null) {
            orderList = privateCache.getFinishOrders(clientId);
        } else {
            orderList = orderMapper.getHistoryOrders(clientId, symbol, orderId, startTime, endTime, limit);
        }
        return orderList == null ? new ArrayList<>() : orderList;
    }

    @Override
    public List<Trade> getHistoryTrades(String clientId, String symbol, Long fromId, Long startTime, Long endTime,
        Long limit) {
        List<Trade> tradeList;
        if (symbol == null && fromId == null && startTime == null && endTime == null) {
            tradeList = privateCache.getTrades(clientId);
        } else {
            tradeList = tradeMapper.getHistoryTrades(clientId, symbol, fromId, startTime, endTime, limit);
        }
        return tradeList == null ? new ArrayList<>() : tradeList;
    }

    @Override
    public List<Bill> getBills(String clientId, String symbol, Long startTime, Long endTime, Long limit) {
        List<Bill> billList;
        if (symbol == null && startTime == null && endTime == null) {
            billList = privateCache.getBills(clientId);
        } else {
            billList = billMapper.getHistoryBills(clientId, symbol, startTime, endTime, limit);
        }
        return billList == null ? new ArrayList<>() : billList;
    }

    @Override
    public FeeRate getFeeRate(String clientId, String symbol) {
        FeeRate feeRate = redisService.getFeeRateByClientId(clientId);
        if (feeRate == null) {
            BigDecimal makerFeeRate =
                new BigDecimal(publicCache.getSystemParameter(SystemParameter.DEFAULT_FEE_RATE_MAKER));
            BigDecimal takerFeeRate =
                new BigDecimal(publicCache.getSystemParameter(SystemParameter.DEFAULT_FEE_RATE_TAKER));
            feeRate = new FeeRate(clientId, makerFeeRate, takerFeeRate);
        }
        return feeRate;
    }

    @Override
    public ClientSetting getClientSetting(String clientId, String symbol) {
        ClientSetting setting = redisService.getSettingsByClientId(clientId, symbol);
        if (setting == null) {
            SymbolInfo symbolInfo = publicCache.getSymbolInfo(symbol);
            setting = new ClientSetting();
            setting.setSymbol(symbol);
            setting.setLeverage(symbolInfo.getDefaultLeverage());
            setting.setMarginType(MarginType.CROSSED);
        }
        return setting;
    }

    /**
     * filter order
     */
    private Order filterOrder(Collection<Order> orders, String symbol, String clientOrderId, Long orderId) {
        List<Order> orderList = orders.stream().filter(order -> symbol == null || symbol.equals(order.getSymbol()))
            .filter(order -> clientOrderId == null || clientOrderId.equals(order.getClientOrderId()))
            .filter(order -> orderId == null || orderId.equals(order.getOrderId())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orderList)) {
            return null;
        } else {
            return orderList.get(0);
        }
    }

}
