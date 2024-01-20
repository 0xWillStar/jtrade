package com.crypto.jtrade.common.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;

/**
 * stream channel
 *
 * @author 0xWill
 */
@Getter
public enum StreamChannel {

    TICKERS("tickers", false),

    TICKER("ticker", true),

    DEPTH("depth", true),

    KLINE("kline", true),

    TRADE("trade", true),

    INDEX_PRICE("indexPrice", true),

    MARK_PRICE("markPrice", true),

    FUNDING_RATE("fundingRate", true),

    BALANCE("balance", false),

    POSITION("position", false),

    ORDER("order", false),

    NOTIFICATION("notification", false);

    private String code;

    private boolean symbolAllowed;

    private static final Map<String, StreamChannel> TYPES;
    static {
        Map<String, StreamChannel> types = new HashMap<>();
        Stream.of(StreamChannel.values()).forEach(type -> types.put(type.getCode(), type));
        TYPES = Collections.unmodifiableMap(types);
    }

    StreamChannel(String code, boolean symbolAllowed) {
        this.code = code;
        this.symbolAllowed = symbolAllowed;
    }

    public static StreamChannel fromCode(String code) {
        StreamChannel streamChannel = TYPES.get(code);
        if (streamChannel == null) {
            // check if kline
            if (StringUtils.contains(code, StreamChannel.KLINE.getCode())) {
                streamChannel = StreamChannel.KLINE;
            }
        }
        return streamChannel;
    }

}
