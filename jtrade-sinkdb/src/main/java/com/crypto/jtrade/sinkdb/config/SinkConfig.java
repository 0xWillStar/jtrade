package com.crypto.jtrade.sinkdb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * sink config
 *
 * @author 0xWill
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "jtrade.sink")
public class SinkConfig {

    private Integer workerCount = 3;

}
