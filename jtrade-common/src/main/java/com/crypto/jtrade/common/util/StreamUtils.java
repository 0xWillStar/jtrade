package com.crypto.jtrade.common.util;

public class StreamUtils {

    /**
     * get json string of streaming, the format is as follows: { "arg": { "channel": "ticker", "symbol": "BTCUSDT" },
     * "data": { "symbol": "BTCUSDT", "lastPrice": "18000" } }
     */
    public static String getJSONString(String argStr, String dataStr) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("{");
        sb.append("\"arg\":").append(argStr);
        sb.append(",");
        sb.append("\"data\":").append(dataStr);
        sb.append("}");
        return sb.toString();
    }

}
