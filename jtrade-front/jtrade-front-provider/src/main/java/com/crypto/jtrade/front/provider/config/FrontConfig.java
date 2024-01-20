package com.crypto.jtrade.front.provider.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * front config
 *
 * @author 0xWill
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "jtrade.front")
public class FrontConfig {

    private String name;

    private String defaultAsset = "USDC";

    private String marketDbSchema = "jtrade_market";

    private String symbolDelimiter = "-";

    private Integer pingIntervalSeconds = 10;

    private Integer flushIntervalMilliSeconds = 50;

    private String publicQueue;

    private Integer publicWebsocketPort = 9505;

    private Integer publicStreamWorkerSize = 3;

    private String privateQueue;

    private Integer privateWebsocketPort = 9506;

    private Integer privateStreamWorkerSize = 3;

    private Integer klineDefaultSize = 200;

    private Integer tradeDefaultSize = 100;

    private Integer tradeDefaultHours = 1;

    private Integer clientHistoryDefaultSize = 100;

    private Integer clientHistoryDefaultDays = 180;

    private Integer apiKeyTimeoutMinutes = 240;

}
