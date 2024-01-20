package com.crypto.jtrade.common.constants;

/**
 * set how long an order will be active before expiration
 *
 * @author 0xWill
 */
public enum TimeInForce {
    /**
     * Good Til Canceled. An order will be on the book unless the order is canceled.
     */
    GTC,

    /**
     * Immediate Or Cancel. An order will try to fill the order as much as it can before the order expires.
     */
    IOC,

    /**
     * Fill or Kill. An order will expire if the full order cannot be filled upon execution.
     */
    FOK,

    /**
     * Good Till Crossing (Post Only)
     */
    GTX
}
