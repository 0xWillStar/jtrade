package com.crypto.jtrade.core.provider.service.rabbitmq;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;

import com.crypto.jtrade.common.constants.Constants;

/**
 * batch sending data of the rabbitmq
 *
 * @author 0xWill
 **/
public abstract class BatchingService {

    private final RabbitTemplate rabbitTemplate;

    private final int maxBatchSize;

    private final String exchange;

    private final String routingKey;

    private int currentSize;

    private List<byte[]> operationList = new ArrayList<>(128);

    public BatchingService(RabbitTemplate rabbitTemplate, int maxBatchSize, String exchange, String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.maxBatchSize = maxBatchSize;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    /**
     * send data to rabbitmq
     */
    public void sendData() {
        if (operationList.size() > 0) {
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_BYTES);
            messageProperties.setContentEncoding(StandardCharsets.UTF_8.name());
            messageProperties.getHeaders().put(MessageProperties.SPRING_BATCH_FORMAT,
                MessageProperties.BATCH_FORMAT_LENGTH_HEADER4);
            messageProperties.getHeaders().put(AmqpHeaders.BATCH_SIZE, operationList.size());
            messageProperties.getHeaders().put(Constants.RABBIT_BATCH_ID, getRabbitBatchId());

            byte[] body = new byte[currentSize];
            ByteBuffer byteBuffer = ByteBuffer.wrap(body);
            for (byte[] bytes : operationList) {
                byteBuffer.putInt(bytes.length);
                byteBuffer.put(bytes);
            }
            Message message = new Message(body, messageProperties);

            // send data
            rabbitTemplate.send(exchange, routingKey, message);

            // init parameters
            currentSize = 0;
            operationList.clear();
        }
    }

    /**
     * try sends data
     */
    public void trySendData() {
        if (operationList.size() >= maxBatchSize) {
            sendData();
        }
    }

    /**
     * add to batch
     */
    public void addToBatch(String data, boolean trySend) {
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        currentSize += 4 + bytes.length;
        operationList.add(bytes);
        if (trySend) {
            trySendData();
        }
    }

    /**
     * get the batch id
     */
    protected abstract Long getRabbitBatchId();

}
