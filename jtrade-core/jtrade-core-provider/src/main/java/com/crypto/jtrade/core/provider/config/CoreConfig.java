package com.crypto.jtrade.core.provider.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.crypto.jtrade.common.constants.ProductType;

import lombok.Data;

/**
 * core config
 *
 * @author 0xWill
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "jtrade.core")
public class CoreConfig {

    private ProductType productType = ProductType.PERPETUAL;

    private String marketDbSchema = "jtrade_market";

    private String symbolDelimiter = "-";

    private Integer matchEngineSize = 8;

    private Integer clientCacheSize = 4096;

    private Integer depthIntervalMilliSeconds = 100;

    private Integer maxDepths = 20;

    private Integer redisLandingIntervalMilliSeconds = 100;

    private Integer redisLandingMaxBatchSize = 100;

    private Integer mysqlLandingIntervalMilliSeconds = 100;

    private Integer mysqlLandingMaxBatchSize = 100;

    private Integer publicPublishIntervalMilliSeconds = 100;

    private Integer publicPublishMaxBatchSize = 100;

    private Integer privatePublishIntervalMilliSeconds = 100;

    private Integer privatePublishMaxBatchSize = 100;

}
