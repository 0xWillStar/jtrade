package com.crypto.jtrade.core.provider.model.landing;

import java.util.List;

import com.crypto.jtrade.common.model.TradeAuthority;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * client trade authority landing
 *
 * @author 0xWill
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientTradeAuthorityLanding {

    private Long requestId;

    private List<TradeAuthority> tradeAuthorityList;

}
