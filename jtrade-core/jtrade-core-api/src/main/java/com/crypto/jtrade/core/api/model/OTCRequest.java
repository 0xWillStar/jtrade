package com.crypto.jtrade.core.api.model;

import java.math.BigDecimal;
import java.util.List;

import com.crypto.jtrade.common.constants.MatchRole;
import com.crypto.jtrade.common.constants.TradeType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OTC request
 *
 * @author 0xWill
 */
@Data
public class OTCRequest {

    private String clientId1;

    private Long expectedTradeId1;

    private String clientId2;

    private Long expectedTradeId2;

    private List<Detail> detailList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Detail {

        private String symbol;

        private BigDecimal price;

        private BigDecimal quantity;

        private String buyClientId;

        private MatchRole buyMatchRole;

        private String sellClientId;

        private MatchRole sellMatchRole;

        private TradeType tradeType;
    }

}
