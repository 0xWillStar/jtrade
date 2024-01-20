package com.crypto.jtrade.core.provider.model.landing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * matched landings, include buy and sell.
 *
 * @author 0xWill
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchedLandings {

    private OrderMatchedLanding buyLanding;

    private OrderMatchedLanding sellLanding;

}
