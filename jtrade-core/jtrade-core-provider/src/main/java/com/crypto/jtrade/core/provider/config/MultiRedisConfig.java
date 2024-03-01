package com.crypto.jtrade.core.provider.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * multi redis config
 *
 * @author 0xWill
 **/
@Configuration
public class MultiRedisConfig {

    @Value("${spring.redis.lettuce.pool.max-active}")
    int maxActive;
    @Value("${spring.redis.lettuce.pool.max-idle}")
    int maxIdle;
    @Value("${spring.redis.lettuce.pool.min-idle}")
    int minIdle;

    @Bean(name = "tradeRedisTemplate")
    public StringRedisTemplate tradeRedisTemplate(@Value("${spring.redis.database:0}") int database,
        @Value("${spring.redis.host}") String hostName, @Value("${spring.redis.port:6379}") int port,
        @Value("${spring.redis.password}") String password) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory(database, hostName, port, password));
        return template;
    }

    @Bean(name = "logRedisTemplate")
    public StringRedisTemplate logRedisTemplate(@Value("${spring.redis-log.database:0}") int database,
        @Value("${spring.redis-log.host}") String hostName, @Value("${spring.redis-log.port:6379}") int port,
        @Value("${spring.redis-log.password}") String password) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory(database, hostName, port, password));
        return template;
    }

    private RedisConnectionFactory connectionFactory(int database, String hostName, int port, String password) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(hostName);
        configuration.setPort(port);
        if (StringUtils.isNotBlank(password)) {
            configuration.setPassword(password);
        }
        if (database != 0) {
            configuration.setDatabase(database);
        }

        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxIdle(maxIdle);
        genericObjectPoolConfig.setMinIdle(minIdle);
        genericObjectPoolConfig.setMaxTotal(maxActive);

        LettuceClientConfiguration clientConfig =
            LettucePoolingClientConfiguration.builder().poolConfig(genericObjectPoolConfig).build();

        LettuceConnectionFactory lettuce = new LettuceConnectionFactory(configuration, clientConfig);
        lettuce.afterPropertiesSet();
        return lettuce;
    }

}
