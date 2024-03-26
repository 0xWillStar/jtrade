package com.crypto.jtrade.core.provider.service.cache;

import java.util.concurrent.ConcurrentMap;

import com.crypto.jtrade.common.model.*;
import com.crypto.jtrade.common.util.recycle.Recyclable;
import com.crypto.jtrade.common.util.recycle.Recyclers;

/**
 * Client information
 *
 * @author 0xWill
 **/
public final class ClientEntity implements Recyclable {

    private static final Recyclers<ClientEntity> recyclers = new Recyclers<ClientEntity>(1024) {

        @Override
        protected ClientEntity newObject(Handle handle) {
            return new ClientEntity(handle);
        }
    };

    private transient Recyclers.Handle handle;

    private String clientId;

    /**
     * All assets balance of the client. KEY：an asset
     */
    private ConcurrentMap<String, AssetBalance> balances;

    /**
     * All positions of the client. KEY：symbol_positionSide, now only symbol
     */
    private ConcurrentMap<String, Position> positions;

    /**
     * All open orders of the client. KEY：orderId
     */
    private ConcurrentMap<Long, Order> orders;

    /**
     * set by the client. KEY：symbol, VALUE：setting
     */
    private ConcurrentMap<String, ClientSetting> settings;

    /**
     * trade fee rate
     */
    private FeeRate feeRate;

    /**
     * Last update time
     */
    private Long lastUpdateTime = System.currentTimeMillis();

    /**
     * Last trade id
     */
    private Long lastTradeId;

    /**
     * trade authority
     */
    private int tradeAuthority;

    /**
     * create a ClientEntity instance
     */
    public static ClientEntity newInstance(final boolean recycled) {
        if (recycled) {
            return recyclers.get();
        } else {
            return new ClientEntity();
        }
    }

    ClientEntity() {
        this(Recyclers.NOOP_HANDLE);
    }

    ClientEntity(final Recyclers.Handle handle) {
        this.handle = handle;
    }

    void clear() {
        this.clientId = null;
        this.balances.clear();
        this.positions.clear();
        this.orders.clear();
        this.settings.clear();
        this.feeRate = null;
        this.lastTradeId = 0L;
        this.tradeAuthority = 0;
    }

    @Override
    public boolean recycle() {
        clear();
        return recyclers.recycle(this, handle);
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void initBalances(ConcurrentMap<String, AssetBalance> balances) {
        this.balances = balances;
    }

    public void addBalance(AssetBalance balance) {
        this.balances.put(balance.getAsset(), balance);
    }

    public AssetBalance getBalance(String asset) {
        return this.balances.get(asset);
    }

    public ConcurrentMap<String, AssetBalance> getBalances() {
        return this.balances;
    }

    public void initPositions(ConcurrentMap<String, Position> positions) {
        this.positions = positions;
    }

    public void addPosition(Position position) {
        this.positions.put(position.getSymbol(), position);
    }

    public Position getPosition(String symbol) {
        return this.positions.get(symbol);
    }

    public ConcurrentMap<String, Position> getPositions() {
        return this.positions;
    }

    public void initOrders(ConcurrentMap<Long, Order> orders) {
        this.orders = orders;
    }

    public void setOrder(Order order) {
        this.orders.put(order.getOrderId(), order);
    }

    public Order getOrderByOrderId(Long orderId) {
        return this.orders.get(orderId);
    }

    public ConcurrentMap<Long, Order> getOrders() {
        return this.orders;
    }

    public void removeOrder(Long orderId) {
        this.orders.remove(orderId);
    }

    public void initSettings(ConcurrentMap<String, ClientSetting> settings) {
        this.settings = settings;
    }

    public void addSetting(ClientSetting setting) {
        this.settings.put(setting.getSymbol(), setting);
    }

    public ClientSetting getSetting(String symbol) {
        return this.settings.get(symbol);
    }

    public ConcurrentMap<String, ClientSetting> getSettings() {
        return this.settings;
    }

    public void setFeeRate(FeeRate feeRate) {
        this.feeRate = feeRate;
    }

    public FeeRate getFeeRate() {
        return this.feeRate;
    }

    public void refreshUpdateTime() {
        this.lastUpdateTime = System.currentTimeMillis();
    }

    public Long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    public Long getLastTradeId() {
        return this.lastTradeId;
    }

    public void setLastTradeId(Long tradeId) {
        this.lastTradeId = tradeId;
    }

    public int getTradeAuthority() {
        return this.tradeAuthority;
    }

    public void setTradeAuthority(int tradeAuthority) {
        this.tradeAuthority = tradeAuthority;
    }

}
