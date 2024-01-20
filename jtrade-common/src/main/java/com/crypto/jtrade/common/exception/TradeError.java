package com.crypto.jtrade.common.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * JTrade error code.
 *
 * @author 0xWill
 */
public enum TradeError {
    
    UNKNOWN(-1, "unknown"),
    SUCCESS(0, "success"),
    
    INTERNAL(1000, "internal error"),
    REQUEST_PROCESS_TIMEOUT(1001, "request processing timed out"),
    REQUEST_TOO_MANY(1002, "too many requests"),
    MATCH_ENGINE_INSUFFICIENT(1003, "insufficient number of match engines"),
    MATCH_ENGINE_NOT_EXIST(1004, "match engine does not exist"),
    SYMBOL_MATCHER_NOT_EXIST(1005, "symbol matcher does not exist"),
    SYMBOL_NOT_EXIST(1006, "symbol does not exist"),
    SYMBOL_CANNOT_TRADED(1007, "symbol cannot be traded"),
    CLIENT_ORDER_ID_DUPLICATE(1008, "clientOrderId is duplicate"),
    ARGUMENT_INVALID(1009, "invalid argument"),
    PRICE_INVALID(1010, "invalid price"),
    QUANTITY_INVALID(1011, "invalid quantity"),
    INSUFFICIENT_FUNDS(1012, "insufficient funds"),
    MARK_PRICE_NOT_EXIST(1013, "mark price does not exist"),
    ORDER_NOT_EXIST(1014, "order does not exist"),
    FORBID_OPEN_POSITION(1015, "open position is forbidden"),
    FORBID_CLOSE_POSITION(1016, "close position is forbidden"),
    FORBID_CANCEL_ORDER(1017, "cancel order is forbidden"),
    HAS_OTHER_TRADE(1018, "there are other trades in liquidation processing"),
    NO_COUNTERPARTY(1019, "no counterparty"),
    REDUCE_ONLY_INVALID(1020, "ReduceOnly is invalid"),
    SYMBOL_STATUS_INVALID(1021, "invalid symbol status"),
    INSUFFICIENT_FUNDS_AVAILABLE(1013, "insufficient funds available"),
    DATA_OBJECT_INVALID(1014, "invalid data object"),
    DATA_ACTION_INVALID(1015, "invalid data action"),
    CHANNEL_NOT_EXIST(1016, "channel does not exist"),
    REQUEST_ILLEGAL(1017, "illegal request"),
    ADDRESS_INVALID(1018, "invalid address"),
    TIMESTAMP_INVALID(1019, "invalid timestamp"),
    SIGNATURE_INVALID(1020, "invalid signature"),
    API_KEY_INVALID(1021, "invalid api key"),
    EXCEEDED_MAX_LEVERAGE(1022, "exceeded maximum leverage"),
    LEVERAGE_INVALID(1023, "invalid leverage"),
    FORBID_CHANGE_MARGIN_TYPE(1024, "change margin type is forbidden"),
    FORBID_ADJUST_POSITION_MARGIN(1025, "adjust position margin is forbidden"),
    TIME_IN_FORCE_INVALID(1026, "TimeInForce is invalid"),
    REJECT_ORDER(1027, "order rejected"),
    OTO_ORDERS_INVALID(1028, "invalid OTO orders"),
    OTO_PRIMARY_ORDERS_INVALID(1029, "invalid OTO primary orders"),
    OTO_PRIMARY_NOT_EXISTS(1030, "OTO primary order does not exist"),
    OTO_PRIMARY_REDUCE_ONLY_INVALID(1031, "OTO primary order can't be Reduce Only"),
    OTO_SECONDARY_ORDERS_INVALID(1032, "invalid OTO secondary orders"),
    OTO_SECONDARY_NOT_EXISTS(1033, "OTO secondary order does not exist"),
    OTO_SECONDARY_REDUCE_ONLY_INVALID(1034, "OTO secondary order must be Reduce Only"),
    OTO_SECONDARY_MARKET_INVALID(1035, "OTO secondary order must be market"),
    OTO_SECONDARY_SYMBOL_INVALID(1036, "the symbol of the secondary order must be consistent with the primary order"),
    OTO_SECONDARY_QUANTITY_INVALID(1037, "the quantity of the secondary order must be consistent with the primary order"),
    OTO_SECONDARY_SIDE_INVALID(1038, "the side of the secondary order must be the opposite of the primary order"),
    ASSET_NOT_EXIST(1039, "asset does not exist"),
    INDEX_PRICE_NOT_EXIST(1040, "index price does not exist"),
    
    
    BLANK(999999, "blank");
    
    private Integer code;
    
    private String message;
    
    TradeError(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
    
    private static final Map<Integer, TradeError> ERROR_MAP;
    
    static {
        Map<Integer, TradeError> errors = new HashMap<>();
        Stream.of(TradeError.values()).forEach(error -> errors.put(error.code, error));
        ERROR_MAP = Collections.unmodifiableMap(errors);
    }
    
    public static TradeError fromCode(String code) {
        return ERROR_MAP.getOrDefault(code, UNKNOWN);
    }
    
    public Integer getCode() {
        return this.code;
    }
    
    public String getMessage() {
        return this.message;
    }
}
