package com.crypto.jtrade.core.provider.service.inner.impl;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.crypto.jtrade.common.constants.*;
import com.crypto.jtrade.common.constants.SystemParameter;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.*;
import com.crypto.jtrade.common.util.BigDecimalUtil;
import com.crypto.jtrade.common.util.TypeUtils;
import com.crypto.jtrade.common.util.myserialize.MyFieldInfo;
import com.crypto.jtrade.common.util.myserialize.MyPropertyNamingStrategy;
import com.crypto.jtrade.core.api.model.CancelOrderRequest;
import com.crypto.jtrade.core.api.model.PlaceOrderRequest;
import com.crypto.jtrade.core.provider.service.cache.RedisService;
import com.crypto.jtrade.core.provider.service.inner.InnerService;
import com.crypto.jtrade.core.provider.service.query.QueryService;
import com.crypto.jtrade.core.provider.service.trade.TradeCommand;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

/**
 * inner service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class InnerServiceImpl implements InnerService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private TradeCommand tradeCommand;

    @Autowired
    private QueryService queryService;

    @Value("${jtrade.log.path}")
    private String logPath;

    @Override
    public Map<SystemParameter, String> getSystemParametersFromRedis() {
        return redisService.getSystemParameters();
    }

    @Override
    public List<SymbolInfo> getSymbolsFromRedis(String symbol) {
        Map<String, SymbolInfo> symbolInfoMap = redisService.getSymbols();
        if (symbol != null) {
            return Lists.newArrayList(symbolInfoMap.get(symbol));
        } else {
            return Lists.newArrayList(symbolInfoMap.values());
        }
    }

    @Override
    public ClientInfo getClientInfoFromRedis(String clientId) {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setBalances(Lists.newArrayList(redisService.getBalancesByClientId(clientId).values()));
        clientInfo.setPositions(Lists.newArrayList(redisService.getPositionsByClientId(clientId).values()));
        clientInfo.setOrders(Lists.newArrayList(redisService.getOrdersByClientId(clientId).values()));
        clientInfo.setSettings(Lists.newArrayList(redisService.getSettingsByClientId(clientId).values()));
        clientInfo.setFeeRate(redisService.getFeeRateByClientId(clientId));
        clientInfo.setTradeAuthority(redisService.getTradeAuthorityByClientId(clientId));
        return clientInfo;
    }

    @Override
    public Set<String> getOrderClientIdsFromRedis() {
        return redisService.getOrderClientIds();
    }

    @Override
    public Set<String> getPositionClientIdsFromRedis() {
        return redisService.getPositionClientIds();
    }

    @Override
    public void cancelClientOrder(String clientId, Long orderId) {
        List<Order> orderList = null;
        ConcurrentMap<Long, Order> orderMap = redisService.getOrdersByClientId(clientId);
        if (orderId == null) {
            orderList = Lists.newArrayList(orderMap.values());
        } else {
            Order order = orderMap.get(orderId);
            if (order != null) {
                orderList = Collections.singletonList(order);
            }
        }

        if (!CollectionUtils.isEmpty(orderList)) {
            orderList.forEach(order -> {
                CancelOrderRequest request = new CancelOrderRequest();
                request.setClientId(clientId);
                request.setSymbol(order.getSymbol());
                request.setOrderId(order.getOrderId());
                tradeCommand.cancelOrder(request);
            });
        }
    }

    @Override
    public void closeClientPosition(String clientId, String symbol) {
        List<Position> positionList = null;
        ConcurrentMap<String, Position> positionMap = redisService.getPositionsByClientId(clientId);
        if (symbol == null) {
            positionList = Lists.newArrayList(positionMap.values());
        } else {
            Position position = positionMap.get(symbol);
            if (position != null) {
                positionList = Collections.singletonList(position);
            }
        }

        if (!CollectionUtils.isEmpty(positionList)) {
            positionList.forEach(this::closePosition);
        }
    }

    @Override
    public void exportOrderBook(String symbol) {
        Depth depth = queryService.getDepth(symbol);
        SymbolInfo symbolInfo = queryService.getSymbol(symbol);
        try (FileOutputStream fos = new FileOutputStream(logPath + "/a-order-book.txt", false);
            FileChannel fc = fos.getChannel()) {
            StringBuilder sb = new StringBuilder(1024);
            sb.append(symbol).append("\n\n");

            // ask
            for (int i = depth.getAsks().size() - 1; i >= 0; i--) {
                Depth.Item item = depth.getAsks().get(i);
                sb.append(BigDecimalUtil.getVal(item.getPrice(), symbolInfo.getPriceAssetScale())).append("\t")
                    .append(item.getQuantity()).append("\n");
            }
            sb.append("\n");
            // bid
            for (int i = 0; i < depth.getBids().size(); i++) {
                Depth.Item item = depth.getBids().get(i);
                sb.append(BigDecimalUtil.getVal(item.getPrice(), symbolInfo.getPriceAssetScale())).append("\t")
                    .append(item.getQuantity()).append("\n");
            }
            appendToFile(fc, sb.toString(), true, false);
        } catch (IOException e) {
            throw new TradeException(e);
        }
    }

    @Override
    public void exportOrders(String clientId, String symbol) {
        Set<String> clientIds = redisService.getOrderClientIds();
        if (CollectionUtils.isEmpty(clientIds)) {
            return;
        }

        List<String> clientIdList = null;
        if (clientId != null) {
            if (clientIds.contains(clientId)) {
                clientIdList = Collections.singletonList(clientId);
            }
        } else {
            clientIdList = Lists.newArrayList(clientIds);
        }
        if (clientIdList == null) {
            return;
        }
        Collections.sort(clientIdList);

        try (FileOutputStream fos = new FileOutputStream(logPath + "/a-orders.txt", false);
            FileChannel fc = fos.getChannel()) {
            List<MyFieldInfo> fieldInfoList =
                TypeUtils.computeFields(Order.class, MyPropertyNamingStrategy.SnakeCase, SerializeType.TEXT);
            appendToFile(fc, genTitle(fieldInfoList), true, true);

            for (String cid : clientIdList) {
                ConcurrentMap<Long, Order> orderMap = redisService.getOrdersByClientId(cid);
                List<Order> orderList = Lists.newArrayList(orderMap.values());
                orderList.sort(Comparator.comparing(Order::getOrderId));
                for (Order order : orderList) {
                    if (symbol != null && !symbol.equals(order.getSymbol())) {
                        continue;
                    }
                    appendToFile(fc, order.toString(), false, true);
                }
                fc.force(false);
            }
        } catch (IOException e) {
            throw new TradeException(e);
        }
    }

    @Override
    public void exportPositions(String clientId, String symbol) {
        Set<String> clientIds = redisService.getPositionClientIds();
        if (CollectionUtils.isEmpty(clientIds)) {
            return;
        }

        List<String> clientIdList = null;
        if (clientId != null) {
            if (clientIds.contains(clientId)) {
                clientIdList = Collections.singletonList(clientId);
            }
        } else {
            clientIdList = Lists.newArrayList(clientIds);
        }
        if (clientIdList == null) {
            return;
        }
        Collections.sort(clientIdList);

        try (FileOutputStream fos = new FileOutputStream(logPath + "/a-positions.txt", false);
            FileChannel fc = fos.getChannel()) {
            List<MyFieldInfo> fieldInfoList =
                TypeUtils.computeFields(Position.class, MyPropertyNamingStrategy.SnakeCase, SerializeType.TEXT);
            appendToFile(fc, genTitle(fieldInfoList), true, true);

            for (String cid : clientIdList) {
                ConcurrentMap<String, Position> positionMap = redisService.getPositionsByClientId(cid);
                List<Position> positionList = Lists.newArrayList(positionMap.values());
                positionList.sort(Comparator.comparing(Position::getSymbol));
                for (Position position : positionList) {
                    if (symbol != null && !symbol.equals(position.getSymbol())) {
                        continue;
                    }
                    appendToFile(fc, position.toString(), false, true);
                }
                fc.force(false);
            }
        } catch (IOException e) {
            throw new TradeException(e);
        }
    }

    private void closePosition(Position position) {
        Depth depth = queryService.getDepth(position.getSymbol());
        List<Depth.Item> bookOrders;
        if (position.getPositionAmt().compareTo(BigDecimal.ZERO) > 0) {
            bookOrders = depth.getBids();
        } else {
            bookOrders = depth.getAsks();
        }
        BigDecimal bookQty = bookOrders.stream().map(Depth.Item::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (bookQty.compareTo(position.getPositionAmt().abs()) < 0) {
            throw new TradeException(TradeError.NO_COUNTERPARTY);
        }

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setClientId(position.getClientId());
        request.setSymbol(position.getSymbol());
        request.setSide(position.getPositionAmt().compareTo(BigDecimal.ZERO) > 0 ? OrderSide.SELL : OrderSide.BUY);
        request.setType(OrderType.MARKET);
        request.setTimeInForce(TimeInForce.IOC);
        request.setQuantity(position.getPositionAmt().abs());
        request.setReduceOnly(true);
        tradeCommand.placeOrder(request);
    }

    private String genTitle(List<MyFieldInfo> fieldInfoList) {
        StringBuilder title = new StringBuilder(1024);
        for (int i = 0; i < fieldInfoList.size(); i++) {
            MyFieldInfo fieldInfo = fieldInfoList.get(i);
            title.append(fieldInfo.name);
            if (i < fieldInfoList.size() - 1) {
                title.append(",");
            }
        }
        return title.toString();
    }

    private void appendToFile(FileChannel fc, String data, boolean force, boolean newLine) throws IOException {
        if (newLine) {
            data += "\n";
        }
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        fc.write(buffer);
        if (force) {
            fc.force(false);
        }
    }

}
