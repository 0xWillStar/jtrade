package com.crypto.jtrade.common.model;

import lombok.Data;

/**
 * argument of streaming
 *
 * @author 0xWill
 **/
@Data
public class StreamArgument {

    private String channel;

    private String symbol;

    private String asset;

    public StreamArgument(String channel) {
        this.channel = channel;
    }

    public StreamArgument(String channel, String symbol) {
        this.channel = channel;
        this.symbol = symbol;
    }

    public StreamArgument(String channel, String symbol, String asset) {
        this.channel = channel;
        this.symbol = symbol;
        this.asset = asset;
    }

    public String toJSONString() {
        StringBuilder sb = new StringBuilder(128);
        boolean first = true;
        sb.append("{");
        if (getChannel() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"channel\":\"").append(getChannel()).append("\"");
        }

        if (getSymbol() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"symbol\":\"").append(getSymbol()).append("\"");
        }

        if (getAsset() != null) {
            if (first) {
                first = false;
            } else {
                sb.append(",");
            }

            sb.append("\"asset\":\"").append(getAsset()).append("\"");
        }

        sb.append("}");
        return sb.toString();
    }

}
