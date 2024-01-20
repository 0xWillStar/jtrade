package com.crypto.jtrade.core.provider.service.rule.impl.perpetual;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.landing.MySqlLanding;
import com.crypto.jtrade.core.provider.service.landing.RedisLanding;
import com.crypto.jtrade.core.provider.service.publish.PrivatePublish;
import com.crypto.jtrade.core.provider.service.rule.impl.AbstractTradeRule;
import com.crypto.jtrade.core.provider.util.SequenceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.OrderSide;
import com.crypto.jtrade.common.constants.OrderStatus;
import com.crypto.jtrade.common.constants.OrderType;
import com.crypto.jtrade.common.constants.PositionSide;
import com.crypto.jtrade.common.model.Bill;
import com.crypto.jtrade.common.model.ComplexEntity;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.core.api.model.OTCRequest;
import com.crypto.jtrade.core.provider.model.landing.MatchedLandings;
import com.crypto.jtrade.core.provider.model.landing.OrderMatchedLanding;
import com.crypto.jtrade.core.provider.model.session.TradeSession;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * OTC trade
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class OTCTradeRule extends AbstractTradeRule {

    @Getter
    private int sequence = 1;

    @Getter
    private long usedProductType = Constants.USE_PERPETUAL;

    @Getter
    private long usedCommand = Constants.USE_OTC;

    @Autowired
    private RedisLanding redisLanding;

    @Autowired
    private PrivatePublish privatePublish;

    @Autowired
    private MySqlLanding mySqlLanding;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private MatchTradeRule matchTradeRule;

    /**
     * OTC trade
     */
    @Override
    public void otcTrade(Long requestId, OTCRequest request) {
        for (OTCRequest.Detail detail : request.getDetailList()) {
            Long tradeId = SequenceHelper.incrementAndGetTradeId();
            /**
             * BUY
             */
            Order buyOrder = createVirtualOrder(detail.getBuyClientId(), detail.getSymbol(), detail.getPrice(),
                detail.getQuantity(), OrderSide.BUY);
            TradeSession buySession = new TradeSession();
            buySession.init(detail.getTradeType(), buyOrder, localCache);
            matchTradeRule.orderMatched(buySession, detail.getPrice(), detail.getQuantity(), detail.getBuyMatchRole(),
                tradeId);

            /**
             * SELL
             */
            Order sellOrder = createVirtualOrder(detail.getSellClientId(), detail.getSymbol(), detail.getPrice(),
                detail.getQuantity(), OrderSide.SELL);
            TradeSession sellSession = new TradeSession();
            sellSession.init(detail.getTradeType(), sellOrder, localCache);
            matchTradeRule.orderMatched(sellSession, detail.getPrice(), detail.getQuantity(), detail.getSellMatchRole(),
                tradeId);

            OrderMatchedLanding buyLanding = buySession.getLanding();
            OrderMatchedLanding sellLanding = sellSession.getLanding();
            MatchedLandings landings = new MatchedLandings(buyLanding, sellLanding);
            /**
             * save result to redis
             */
            redisLanding.orderMatched(landings);
            /**
             * save result to mysql
             */
            mySqlLanding.orderMatched(landings);
            /**
             * private publish
             */
            List<Bill> buyBillList = new ArrayList<>();
            buyBillList.add(buyLanding.getProfitBill());
            buyBillList.add(buyLanding.getFeeBill());
            ComplexEntity buyComplexEntity =
                new ComplexEntity(buyLanding.getOrder(), Collections.singletonList(buyLanding.getBalance()),
                    buyLanding.getPosition(), buyLanding.getTrade(), buyBillList);
            privatePublish.publishComplex(buyComplexEntity);

            List<Bill> sellBillList = new ArrayList<>();
            sellBillList.add(sellLanding.getProfitBill());
            sellBillList.add(sellLanding.getFeeBill());
            ComplexEntity sellComplexEntity =
                new ComplexEntity(sellLanding.getOrder(), Collections.singletonList(sellLanding.getBalance()),
                    sellLanding.getPosition(), sellLanding.getTrade(), sellBillList);
            privatePublish.publishComplex(sellComplexEntity);
        }
    }

    /**
     * create a virtual order
     */
    private Order createVirtualOrder(String clientId, String symbol, BigDecimal price, BigDecimal quantity,
        OrderSide side) {
        Order order = new Order();
        order.setClientId(clientId);
        order.setSymbol(symbol);
        order.setPrice(price);
        order.setQuantity(quantity);
        order.setExecutedQty(quantity);
        order.setSide(side);
        order.setPositionSide(PositionSide.NET);
        order.setType(OrderType.LIMIT);
        order.setStatus(OrderStatus.FILLED);
        return order;
    }

}
