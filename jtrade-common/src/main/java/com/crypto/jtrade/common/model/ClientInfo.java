package com.crypto.jtrade.common.model;

import java.util.List;

import lombok.Data;

/**
 * Client information
 *
 * @author 0xWill
 **/
@Data
public class ClientInfo {

    private List<AssetBalance> balances;

    private List<Position> positions;

    private List<Order> orders;

    private List<ClientSetting> settings;

    private FeeRate feeRate;

    private Integer tradeAuthority;

}
