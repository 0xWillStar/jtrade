package com.crypto.jtrade.front.provider.config;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.rabbit.stream.config.StreamRabbitListenerContainerFactory;
import org.springframework.rabbit.stream.listener.StreamListenerContainer;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.OffsetSpecification;

import lombok.extern.slf4j.Slf4j;

/**
 * rabbit mq config
 *
 * @author 0xWill
 **/
@Configuration
@Slf4j
public class RabbitMqConfig {

    @Value("${jtrade.front.public-consumer-name}")
    private String publicConsumerName;

    @Value("${jtrade.front.private-consumer-name}")
    private String privateConsumerName;

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

    @Bean("publicStreamListenerFactory")
    public RabbitListenerContainerFactory<StreamListenerContainer> publicStreamListenerFactory(Environment env) {
        StreamRabbitListenerContainerFactory factory = new StreamRabbitListenerContainerFactory(env);
        factory.setConsumerCustomizer((id, builder) -> {
            builder.name(publicConsumerName).offset(OffsetSpecification.next()).autoTrackingStrategy();
        });
        return factory;
    }

    @Bean("privateStreamListenerFactory")
    public RabbitListenerContainerFactory<StreamListenerContainer> privateStreamListenerFactory(Environment env) {
        StreamRabbitListenerContainerFactory factory = new StreamRabbitListenerContainerFactory(env);
        factory.setConsumerCustomizer((id, builder) -> {
            builder.name(privateConsumerName).offset(OffsetSpecification.next()).autoTrackingStrategy();
        });
        return factory;
    }

}
