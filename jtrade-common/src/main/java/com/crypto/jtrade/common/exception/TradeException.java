package com.crypto.jtrade.common.exception;

import lombok.Data;

/**
 * JTrade exception
 *
 * @author 0xWill
 */
@Data
public class TradeException extends RuntimeException {

    private static final long serialVersionUID = 4553278940208270292L;

    private Integer code;

    public TradeException() {

    }

    public TradeException(String message) {
        super(message);
        this.code = TradeError.INTERNAL.getCode();
    }

    public TradeException(Throwable cause) {
        super(cause);
        this.code = TradeError.INTERNAL.getCode();
    }

    public TradeException(String message, Throwable cause) {
        super(message, cause);
        this.code = TradeError.INTERNAL.getCode();
    }

    public TradeException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public TradeException(TradeError error) {
        super(error.getMessage());
        this.code = error.getCode();
    }

}
