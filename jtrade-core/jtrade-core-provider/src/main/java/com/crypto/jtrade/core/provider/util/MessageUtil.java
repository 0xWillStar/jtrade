package com.crypto.jtrade.core.provider.util;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import org.springframework.rabbit.stream.support.StreamMessageProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageUtil {

    /**
     * build stream message
     * 
     * @param data data of message
     * @return Message
     */
    public static Message buildStreamMessage(String data) {
        StreamMessageProperties messageProperties = new StreamMessageProperties();
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_BYTES);
        messageProperties.setContentEncoding(StandardCharsets.UTF_8.name());
        return new Message(data.getBytes(StandardCharsets.UTF_8), messageProperties);
    }

    /**
     * rabbit stream send
     * 
     * @param streamTemplate RabbitStreamTemplate
     * @param data data of message
     */
    public static void send(RabbitStreamTemplate streamTemplate, String data) {
        CompletableFuture<Boolean> future = streamTemplate.send(buildStreamMessage(data)).completable();
        future.exceptionally(e -> {
            log.error("rabbit stream send message error: {}", data, e);
            return null;
        });
    }

}
