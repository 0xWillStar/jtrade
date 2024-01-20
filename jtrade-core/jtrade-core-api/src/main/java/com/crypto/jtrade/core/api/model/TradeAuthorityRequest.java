package com.crypto.jtrade.core.api.model;

import com.crypto.jtrade.common.model.TradeAuthority;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * trade authority request
 *
 * @author 0xWill
 */
@Data
@NoArgsConstructor
public class TradeAuthorityRequest extends TradeAuthority {

    public TradeAuthorityRequest(String clientId, Integer tradeAuthority) {
        setClientId(clientId);
        setTradeAuthority(tradeAuthority);
    }

    public String toString() {
        return super.toString();
    }

}
