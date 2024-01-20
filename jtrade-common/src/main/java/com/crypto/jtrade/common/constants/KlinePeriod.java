package com.crypto.jtrade.common.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import lombok.Getter;

/**
 * kline period
 *
 * @author 0xWill
 */
@Getter
public enum KlinePeriod {

    ONE_MIN("1m", 60L, null, "kline1m"),

    FIVE_MIN("5m", 5 * 60L, "1m", "kline5m"),

    FIFTEEN_MIN("15m", 15 * 60L, "5m", "kline15m"),

    THIRTY_MIN("30m", 30 * 60L, "15m", "kline30m"),

    ONE_HOUR("1h", 60 * 60L, "30m", "kline1h"),

    FOUR_HOUR("4h", 4 * 60 * 60L, "1h", "kline4h"),

    ONE_DAY("1d", 24 * 60 * 60L, "4h", "kline1d");

    private String period;

    private Long seconds;

    private String basePeriod;

    private String channelName;

    private static final Map<String, KlinePeriod> TYPES;
    private static final Map<String, KlinePeriod> CHANNELS;
    static {
        Map<String, KlinePeriod> types = new HashMap<>();
        Map<String, KlinePeriod> channels = new HashMap<>();
        Stream.of(KlinePeriod.values()).forEach(type -> {
            types.put(type.period, type);
            channels.put(type.channelName, type);
        });
        TYPES = Collections.unmodifiableMap(types);
        CHANNELS = Collections.unmodifiableMap(channels);
    }

    KlinePeriod(String period, Long seconds, String basePeriod, String channelName) {
        this.period = period;
        this.seconds = seconds;
        this.basePeriod = basePeriod;
        this.channelName = channelName;
    }

    public static KlinePeriod fromPeriod(String period) {
        return TYPES.getOrDefault(period, null);
    }

    public static KlinePeriod fromChannelName(String channelName) {
        return CHANNELS.getOrDefault(channelName, null);
    }

}
