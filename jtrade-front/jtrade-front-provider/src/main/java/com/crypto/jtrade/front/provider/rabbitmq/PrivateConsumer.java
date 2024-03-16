package com.crypto.jtrade.front.provider.rabbitmq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.StreamChannel;
import com.crypto.jtrade.common.model.*;
import com.crypto.jtrade.common.util.StreamUtils;
import com.crypto.jtrade.front.provider.cache.PrivateCache;
import com.crypto.jtrade.front.provider.config.FrontConfig;
import com.crypto.jtrade.front.provider.model.StreamRequestArg;
import com.crypto.jtrade.front.provider.websocket.service.PrivateStreamManager;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/**
 * batching consume from RabbitMq, only one consumer thread.
 *
 * @author 0xWill
 **/
@Slf4j
@Component
public class PrivateConsumer {

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Autowired
    private FrontConfig frontConfig;

    @Autowired
    private PrivateCache privateCache;

    @Autowired
    private PrivateStreamManager privateStreamManager;

    @Value("${spring.rabbitmq.listener.type}")
    private String listenerType;

    @PostConstruct
    public void init() {
        if (!"stream".equals(listenerType)) {
            try (Connection connection = connectionFactory.createConnection();
                Channel channel = connection.createChannel(false)) {
                // delete old private messages
                channel.queuePurge(frontConfig.getPrivateQueue());
            } catch (TimeoutException | IOException e) {
                log.error("purging private message error: {}", e.getMessage(), e);
            }
        }
    }

    @RabbitListener(queues = Constants.MQ_CORE_STREAM_PRIVATE, containerFactory = "privateStreamListenerFactory")
    public void onMessageStream(String message) {
        if (StringUtils.isEmpty(message)) {
            return;
        }
        ComplexEntity complexEntity = JSONObject.parseObject(message, new TypeReference<ComplexEntity>() {});
        if (complexEntity != null) {
            handleMessage(complexEntity);
        }
    }

    // @RabbitListener(queues = "${jtrade.front.private-queue}", containerFactory =
    // "batchRabbitListenerContainerFactory")
    // public void onMessageBatch(List<Message> list, Channel channel) throws IOException {
    // if (CollectionUtils.isEmpty(list)) {
    // return;
    // }
    // for (Message message : list) {
    // String str = new String(message.getBody(), StandardCharsets.UTF_8);
    // ComplexEntity complexEntity = JSONObject.parseObject(str, new TypeReference<ComplexEntity>() {});
    // if (complexEntity != null) {
    // handleMessage(complexEntity);
    // }
    // }
    // }

    /**
     * handle message
     */
    private void handleMessage(ComplexEntity complexEntity) {
        if (!CollectionUtils.isEmpty(complexEntity.getBalanceList())) {
            for (AssetBalance balance : complexEntity.getBalanceList()) {
                privateCache.setBalance(balance);
                pushData(balance.getClientId(), StreamChannel.BALANCE.getCode(), balance.toJSONString());
            }
        }

        Position position = complexEntity.getPosition();
        if (position != null) {
            privateCache.setPosition(position);
            pushData(position.getClientId(), StreamChannel.POSITION.getCode(), position.toJSONString());
        }

        Order order = complexEntity.getOrder();
        if (order != null) {
            privateCache.setOrder(order);
            pushData(order.getClientId(), StreamChannel.ORDER.getCode(), order.toJSONString());
        }

        if (complexEntity.getTrade() != null) {
            privateCache.setTrade(complexEntity.getTrade());
        }
        if (!CollectionUtils.isEmpty(complexEntity.getBillList())) {
            for (Bill bill : complexEntity.getBillList()) {
                privateCache.setBill(bill);
            }
        }
    }

    /**
     * push data
     */
    private void pushData(String clientId, String channel, String value) {
        // get topic
        StreamRequestArg streamRequestArg = new StreamRequestArg(channel, null);
        String topic = streamRequestArg.toTopicString(clientId);
        // get data
        StreamArgument argument = new StreamArgument(channel);
        String data = StreamUtils.getJSONString(argument.toJSONString(), value);
        // push
        privateStreamManager.push(clientId, topic, data);
    }

}
