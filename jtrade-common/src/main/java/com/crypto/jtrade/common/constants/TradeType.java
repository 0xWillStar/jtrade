package com.crypto.jtrade.common.constants;

/**
 * trade type
 *
 * @author 0xWill
 */
public enum TradeType {
    /**
     * normal trade
     */
    STANDARD,

    /**
     * Positions are liquidated
     */
    LIQUIDATION,

    /**
     * Positions are auto deleveraging
     */
    AUTO_DELEVERAGING,

    /**
     * WASH ONLY
     */
    WASH_ONLY
}
