package com.crypto.jtrade.core.provider.model.session;

import java.math.BigDecimal;

import com.crypto.jtrade.common.constants.DataAction;
import com.crypto.jtrade.common.constants.MarginType;
import com.crypto.jtrade.common.constants.PositionSide;
import com.crypto.jtrade.common.constants.ProductType;
import com.crypto.jtrade.common.constants.TradeType;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.FeeRate;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.common.model.Trade;
import com.crypto.jtrade.core.provider.model.landing.OrderMatchedLanding;
import com.crypto.jtrade.core.provider.service.cache.ClientEntity;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;

import lombok.Data;

/**
 * OrderSession, context for the current session
 *
 * @author 0xWill
 **/
@Data
public class TradeSession {

    private TradeType tradeType;

    private SymbolInfo symbolInfo;

    private AssetBalance assetBalance;

    private Position position;

    private Order order;

    private Trade trade;

    private FeeRate feeRate;

    private ClientEntity clientEntity;

    private BigDecimal unfreezeMargin;

    private BigDecimal unfreezeFee;

    private OrderMatchedLanding landing;

    private DataAction positionAction;

    private DataAction balanceAction;

    /**
     * init session, each field must be initialized
     */
    public void init(TradeType tradeType, Order order, LocalCacheService localCache) {
        this.tradeType = tradeType;
        this.symbolInfo = localCache.getSymbolInfo(order.getSymbol());
        this.clientEntity = localCache.getClientEntity(order.getClientId());

        this.assetBalance = this.clientEntity.getBalance(this.symbolInfo.getClearAsset());
        if (this.assetBalance == null) {
            this.assetBalance = AssetBalance.createAssetBalance(clientEntity.getClientId(), symbolInfo.getClearAsset(),
                BigDecimal.ZERO);
            this.clientEntity.addBalance(this.assetBalance);
            this.balanceAction = DataAction.INSERT;
        } else {
            this.balanceAction = DataAction.UPDATE;
        }

        if (localCache.getProductType() != ProductType.SPOT) {
            this.position = this.clientEntity.getPosition(order.getSymbol());
            if (this.position == null) {
                this.position = Position.createPosition(clientEntity.getClientId(), symbolInfo.getSymbol(),
                    MarginType.CROSSED, symbolInfo.getClearAsset(), PositionSide.NET);
                this.clientEntity.addPosition(this.position);
                this.positionAction = DataAction.INSERT;
            } else {
                this.positionAction = DataAction.UPDATE;
            }
        }

        this.feeRate = this.clientEntity.getFeeRate();
        this.order = order;
        this.trade = null;
        this.unfreezeMargin = BigDecimal.ZERO;
        this.unfreezeFee = BigDecimal.ZERO;
        this.landing = null;
        // refresh updateTime of the clientEntity
        clientEntity.refreshUpdateTime();
    }

}
