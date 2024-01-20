package com.crypto.jtrade.core.provider.model.convert;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ValueMapping;
import org.mapstruct.ValueMappings;
import org.mapstruct.control.NoComplexMapping;

import com.crypto.jtrade.common.constants.OrderType;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.Kline;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.Ticker;
import com.crypto.jtrade.core.api.model.CancelOrderRequest;
import com.crypto.jtrade.core.api.model.PlaceOrderRequest;

/**
 * Mapping between Java Beans
 *
 * @author 0xWill
 **/
@Mapper(componentModel = "spring", mappingControl = DirectControl.class)
public interface BeanMapping {

    /**
     * Convert PlaceOrderRequest to Order
     */
    @Mapping(source = "request.type", target = "origType")
    Order convert(PlaceOrderRequest request);

    /**
     * Convert Order to PlaceOrderRequest
     */
    @Mapping(source = "order.type", target = "type", mappingControl = NoComplexMapping.class)
    PlaceOrderRequest convert(Order order);

    /**
     * Convert Order to CancelOrderRequest
     */
    CancelOrderRequest convertCancel(Order order);

    /**
     * Convert OrderType when stop order is triggered
     */
    @ValueMappings({@ValueMapping(source = "STOP", target = "LIMIT"),
        @ValueMapping(source = "TAKE_PROFIT", target = "LIMIT"),
        @ValueMapping(source = "STOP_MARKET", target = "MARKET"),
        @ValueMapping(source = "TAKE_PROFIT_MARKET", target = "MARKET")})
    OrderType convertOrderType(OrderType orderType);

    /**
     * clone Order
     */
    Order clone(Order order);

    /**
     * clone position
     */
    Position clone(Position position);

    /**
     * clone balance
     */
    AssetBalance clone(AssetBalance balance);

    /**
     * clone Ticker
     */
    Ticker clone(Ticker ticker);

    /**
     * clone Kline
     */
    Kline clone(Kline kline);

}
