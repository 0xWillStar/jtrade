package com.crypto.jtrade.front.provider.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * rabbit mq config
 *
 * @author 0xWill
 **/
@Configuration
@Slf4j
public class RabbitMqConfig {

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Bean("batchRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory batchRabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConsumerBatchEnabled(true);
        factory.setBatchListener(true);
        factory.setAcknowledgeMode(AcknowledgeMode.NONE);
        return factory;
    }

}
