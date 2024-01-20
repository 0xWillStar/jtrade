package com.crypto.jtrade.sinkdb.service.rabbitmq;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.sinkdb.service.worker.MySqlWorker;
import com.crypto.jtrade.sinkdb.service.worker.WorkerManager;
import com.rabbitmq.client.Channel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * batching consume from RabbitMq
 *
 * @author 0xWill
 **/
@Slf4j
@Component
public class BatchConsumer {

    @Autowired
    private WorkerManager workerManager;

    /**
     * KEY：batchId
     */
    private ConcurrentHashMap<Long, BatchMeta> batchMetaMap = new ConcurrentHashMap<>(128);

    @RabbitListener(queues = Constants.MQ_QUEUE_MYSQL, containerFactory = "batchRabbitListenerContainerFactory")
    public void onMessageBatch(List<Message> list, Channel channel) throws IOException {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        MessageProperties properties = list.get(0).getMessageProperties();
        long batchId = properties.getHeader(Constants.RABBIT_BATCH_ID);
        long tag = properties.getDeliveryTag();
        Map<MySqlWorker, List<String>> messageGroup = new HashMap<>();
        /**
         * Grouping the message to the specified worker according to the clientId.
         */
        String tempClientId = null;
        MySqlWorker tempWorker = null;
        for (Message message : list) {
            String str = new String(message.getBody(), StandardCharsets.UTF_8);
            String[] dataArr = splitMessage(str);
            String clientId = dataArr[0];
            if (!clientId.equals(tempClientId)) {
                tempWorker = workerManager.getMySqlWorker(clientId);
                tempClientId = clientId;
            }
            List<String> messageList = messageGroup.get(tempWorker);
            if (messageList == null) {
                messageList = new ArrayList<>();
                messageGroup.put(tempWorker, messageList);
            }
            messageList.add(dataArr[1]);
        }
        /**
         * record the batch meta
         */
        batchMetaMap.put(batchId, new BatchMeta(channel, tag, new AtomicInteger(messageGroup.size())));
        /**
         * Distributed to each worker for processing
         */
        for (Map.Entry<MySqlWorker, List<String>> entry : messageGroup.entrySet()) {
            MySqlWorker worker = entry.getKey();
            List<String> messageList = entry.getValue();
            worker.appendEntity(batchId, messageList, new MessageStableClosure());
        }
    }

    /**
     * split the message into 2 parts. Messages are separated by commas: the first part is the clientId, the second part
     * is the remainder.
     */
    private String[] splitMessage(String message) {
        int len = message.length();
        int i = 0;
        while (i < len) {
            if (message.charAt(i) == Constants.COMMA) {
                return new String[] {message.substring(0, i), message.substring(i + 1)};
            }
            i++;
        }
        return new String[] {message, Constants.EMPTY};
    }

    class MessageStableClosure implements StableClosure {

        public MessageStableClosure() {

        }

        @Override
        public void finishAt(long batchId, int workerId) {
            BatchMeta batchMeta = batchMetaMap.get(batchId);
            if (batchMeta != null) {
                if (batchMeta.getWorkingCount().decrementAndGet() == 0) {
                    try {
                        batchMeta.getChannel().basicAck(batchMeta.getTag(), false);
                    } catch (IOException e) {
                        log.error("rabbitmq basicAck error", e);
                    }
                    batchMetaMap.remove(batchId);
                }
            }
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class BatchMeta {

        private Channel channel;

        private long tag;

        private AtomicInteger workingCount;

    }

}
