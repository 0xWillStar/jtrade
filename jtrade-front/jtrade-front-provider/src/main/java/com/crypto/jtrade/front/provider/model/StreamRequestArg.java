package com.crypto.jtrade.front.provider.model;

import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.StreamChannel;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * stream argument
 *
 * @author 0xWill
 **/
@Data
@NoArgsConstructor
public class StreamRequestArg {

    private String channel;

    private String symbol;

    private String apiKey;

    private String timestamp;

    private String signature;

    public StreamRequestArg(String channel, String symbol) {
        this.channel = channel;
        this.symbol = symbol;
    }

    public String toTopicString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append(this.channel);
        StreamChannel streamChannel = StreamChannel.fromCode(this.channel);
        if (streamChannel.isSymbolAllowed()) {
            sb.append(Constants.UNDER_LINE);
            sb.append(this.symbol);
        }
        return sb.toString();
    }

    public String toTopicString(String clientId) {
        StringBuilder sb = new StringBuilder(128);
        sb.append(clientId).append(Constants.UNDER_LINE).append(toTopicString());
        return sb.toString();
    }

}
