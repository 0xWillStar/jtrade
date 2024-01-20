package com.crypto.jtrade.front.provider.config.remote;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.crypto.jtrade.core.api.QueryApi;
import com.crypto.jtrade.core.api.TradeApi;

import feign.Feign;
import feign.Logger;
import feign.Retryer;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.querymap.BeanQueryMapEncoder;
import feign.slf4j.Slf4jLogger;

/**
 * trade core api client config
 *
 * @author 0xWill
 **/
@Configuration
public class TradeCoreClientConfig {

    @Value("${jtrade.remote.trade-core-server-url}")
    private String tradeCoreServerUrl;

    @Bean
    public QueryApi queryApi() {
        return Feign.builder().encoder(new JacksonEncoder()).decoder(new JacksonDecoder()).client(new OkHttpClient())
            .retryer(new Retryer.Default(100L, 1000L, 3)).logger(new Slf4jLogger()).logLevel(Logger.Level.FULL)
            .queryMapEncoder(new BeanQueryMapEncoder()).target(QueryApi.class, tradeCoreServerUrl);
    }

    @Bean
    public TradeApi tradeApi() {
        return Feign.builder().encoder(new JacksonEncoder()).decoder(new JacksonDecoder()).client(new OkHttpClient())
            .retryer(new Retryer.Default(100L, 1000L, 3)).logger(new Slf4jLogger()).logLevel(Logger.Level.FULL)
            .queryMapEncoder(new BeanQueryMapEncoder()).target(TradeApi.class, tradeCoreServerUrl);
    }

}
