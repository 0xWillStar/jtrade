package com.crypto.jtrade.core.provider.model.session;

import java.math.BigDecimal;
import java.util.Map;

import com.crypto.jtrade.common.constants.DataAction;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.ClientSetting;
import com.crypto.jtrade.common.model.Depth;
import com.crypto.jtrade.common.model.FeeRate;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.model.Position;
import com.crypto.jtrade.common.model.SymbolIndicator;
import com.crypto.jtrade.common.model.SymbolInfo;
import com.crypto.jtrade.core.provider.service.cache.ClientEntity;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.cache.StopOrderCache;

import lombok.Data;

/**
 * OrderSession, context for the current session
 *
 * @author 0xWill
 **/
@Data
public class OrderSession {

    private Long requestId;

    /**
     * KEY: symbol
     */
    private Map<String, SymbolInfo> allSymbols;

    /**
     * KEY: symbol
     */
    private Map<String, SymbolIndicator> symbolIndicators;

    private AssetBalance assetBalance;

    /**
     * KEY: symbol
     */
    private Map<String, Position> positions;

    /**
     * KEY: orderId
     */
    private Map<Long, Order> orders;

    /**
     * KEY: symbol
     */
    private Map<String, ClientSetting> settings;

    private FeeRate feeRate;

    private int tradeAuthority;

    private ClientEntity clientEntity;

    private Depth lastDepth;

    private StopOrderCache stopOrderCache;

    private Order order;

    private boolean stopTriggered;

    private boolean stopRejected;

    /**
     * used for OTO order
     */
    private Long subOrderId1;
    /**
     * used for OTO order
     */
    private Long subOrderId2;

    private DataAction balanceAction;

    /**
     * init session, each field must be initialized
     */
    public void init(String clientId, String symbol, LocalCacheService localCache, Long requestId) {
        this.requestId = requestId;
        this.symbolIndicators = localCache.getSymbolIndicators();
        this.allSymbols = localCache.getAllSymbols();
        if (!this.allSymbols.containsKey(symbol)) {
            throw new TradeException(TradeError.SYMBOL_NOT_EXIST);
        }
        this.clientEntity = localCache.getClientEntity(clientId);
        String clearAsset = allSymbols.get(symbol).getClearAsset();
        this.assetBalance = clientEntity.getBalance(clearAsset);
        if (this.assetBalance == null) {
            this.assetBalance =
                AssetBalance.createAssetBalance(clientEntity.getClientId(), clearAsset, BigDecimal.ZERO);
            this.clientEntity.addBalance(this.assetBalance);
            this.balanceAction = DataAction.INSERT;
        } else {
            this.balanceAction = DataAction.UPDATE;
        }

        this.positions = clientEntity.getPositions();
        this.orders = clientEntity.getOrders();
        this.settings = clientEntity.getSettings();
        this.feeRate = clientEntity.getFeeRate();
        this.tradeAuthority = clientEntity.getTradeAuthority();
        this.lastDepth = localCache.getLastDepth(symbol);
        this.stopOrderCache = localCache.getStopOrderCache(symbol);
        // refresh updateTime of the clientEntity
        clientEntity.refreshUpdateTime();

        this.order = null;
        this.stopTriggered = false;
        this.stopRejected = false;

        this.subOrderId1 = null;
        this.subOrderId2 = null;
    }

}
