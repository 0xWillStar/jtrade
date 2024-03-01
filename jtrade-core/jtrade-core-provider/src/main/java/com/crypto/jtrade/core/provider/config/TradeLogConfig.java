package com.crypto.jtrade.core.provider.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * core config
 *
 * @author 0xWill
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "jtrade.trade-log")
public class TradeLogConfig {

    private String filePath = "./command";

    private String filePrefix = "command";

    private Long fileMaxSizeMb = 500L;

    private Long forceIntervalMilliSeconds = 5000L;

    private Long forceMaxSizeKb = 100L;

}
