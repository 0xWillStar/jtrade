package com.crypto.jtrade.core.provider.service.rabbitmq;

import java.time.Duration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitStreamTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import org.springframework.rabbit.stream.support.StreamAdmin;

import com.crypto.jtrade.common.constants.Constants;
import com.rabbitmq.stream.Environment;

/**
 * rabbit mq config
 *
 * @author 0xWill
 **/
@Configuration
public class RabbitMqConfig {

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Bean
    public RabbitTemplate rabbitTemplate() {
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.NONE);
        connectionFactory.setPublisherReturns(false);
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(new SimpleMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public Queue mysqlQueue() {
        return new Queue(Constants.MQ_QUEUE_MYSQL, true);
    }

    @Bean
    public DirectExchange mysqlExchange() {
        return new DirectExchange(Constants.MQ_EXCHANGE_MYSQL, true, false);
    }

    @Bean
    public Binding mysqlBinding() {
        return BindingBuilder.bind(mysqlQueue()).to(mysqlExchange()).with(Constants.MQ_ROUTING_MYSQL);
    }

    @Bean
    public Queue publicStreamQueue() {
        return new Queue(Constants.MQ_QUEUE_STREAM_PUBLIC, true);
    }

    @Bean
    public FanoutExchange publicStreamExchange() {
        return new FanoutExchange(Constants.MQ_EXCHANGE_STREAM_PUBLIC, true, false);
    }

    @Bean
    public Binding publicStreamBinding() {
        return BindingBuilder.bind(publicStreamQueue()).to(publicStreamExchange());
    }

    @Bean
    public Queue privateStreamQueue() {
        return new Queue(Constants.MQ_QUEUE_STREAM_PRIVATE, true);
    }

    @Bean
    public FanoutExchange privateStreamExchange() {
        return new FanoutExchange(Constants.MQ_EXCHANGE_STREAM_PRIVATE, true, false);
    }

    @Bean
    public Binding privateStreamBinding() {
        return BindingBuilder.bind(privateStreamQueue()).to(privateStreamExchange());
    }

    @Bean
    public StreamAdmin streamAdmin(Environment env) {
        return new StreamAdmin(env, sc -> {
            sc.stream(Constants.MQ_CORE_STREAM_PUBLIC).maxAge(Duration.ofMinutes(3)).create();
            sc.stream(Constants.MQ_CORE_STREAM_PRIVATE).maxAge(Duration.ofMinutes(3)).create();
        });
    }

    @Bean("publicStreamTemplate")
    public RabbitStreamTemplate publicStreamTemplate(Environment rabbitStreamEnvironment, RabbitProperties properties,
        RabbitStreamTemplateConfigurer configurer) {
        RabbitStreamTemplate template =
            new RabbitStreamTemplate(rabbitStreamEnvironment, Constants.MQ_CORE_STREAM_PUBLIC);
        configurer.configure(template);
        return template;
    }

    @Bean("privateStreamTemplate")
    public RabbitStreamTemplate privateStreamTemplate(Environment rabbitStreamEnvironment, RabbitProperties properties,
        RabbitStreamTemplateConfigurer configurer) {
        RabbitStreamTemplate template =
            new RabbitStreamTemplate(rabbitStreamEnvironment, Constants.MQ_CORE_STREAM_PRIVATE);
        configurer.configure(template);
        return template;
    }

}
