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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.KlinePeriod;
import com.crypto.jtrade.common.constants.StreamChannel;
import com.crypto.jtrade.front.provider.cache.PublicCache;
import com.crypto.jtrade.front.provider.config.FrontConfig;
import com.crypto.jtrade.front.provider.model.StreamRequestArg;
import com.crypto.jtrade.front.provider.websocket.service.PublicStreamManager;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/**
 * batching consume from RabbitMq, only one consumer thread.
 *
 * @author 0xWill
 **/
@Slf4j
@Component
public class PublicConsumer {

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Autowired
    private FrontConfig frontConfig;

    @Autowired
    private PublicCache publicCache;

    @Autowired
    private PublicStreamManager publicStreamManager;

    @Value("${spring.rabbitmq.listener.type}")
    private String listenerType;

    @PostConstruct
    public void init() {
        if (!"stream".equals(listenerType)) {
            try (Connection connection = connectionFactory.createConnection();
                Channel channel = connection.createChannel(false)) {
                // delete old public messages
                channel.queuePurge(frontConfig.getPublicQueue());
            } catch (TimeoutException | IOException e) {
                log.error("public consumer init error: {}", e.getMessage(), e);
            }
        }
        // init kline and trade cache
        publicCache.initKlineCache();
        publicCache.initTradeCache();
    }

    @RabbitListener(queues = Constants.MQ_CORE_STREAM_PUBLIC, containerFactory = "publicStreamListenerFactory")
    public void onMessageStream(String message) {
        if (StringUtils.isEmpty(message)) {
            return;
        }
        /**
         * the json format is as follows: { "arg": { "channel": "ticker", "symbol": "ETH-USDC" }, "data": { "lastPrice":
         * 1918, "lastQty": 0.1, "openPrice": 1918, ...... } }
         */
        JSONObject jsonMessage = JSONObject.parseObject(message);
        JSONObject jsonArg = jsonMessage.getJSONObject("arg");
        String symbol = jsonArg.getString("symbol");
        String origStreamChannel = jsonArg.getString("channel");
        StreamChannel streamChannel = StreamChannel.fromCode(origStreamChannel);
        Object data;
        if (streamChannel == StreamChannel.KLINE) {
            data = jsonMessage.getJSONArray("data");
        } else {
            data = jsonMessage.getJSONObject("data");
        }
        // update local cache
        updateLocalCache(symbol, streamChannel, origStreamChannel, data);
        // websocket push
        StreamRequestArg streamRequestArg = new StreamRequestArg(origStreamChannel, symbol);
        publicStreamManager.push(streamRequestArg.toTopicString(), message);
    }

    // @RabbitListener(queues = "${jtrade.front.public-queue}", containerFactory =
    // "batchRabbitListenerContainerFactory")
    // public void onMessageBatch(List<Message> list, Channel channel) throws IOException {
    // if (CollectionUtils.isEmpty(list)) {
    // return;
    // }
    // for (Message message : list) {
    // String str = new String(message.getBody(), StandardCharsets.UTF_8);
    // /**
    // * the json format is as follows: { "arg": { "channel": "ticker", "symbol": "ETH-USDC" }, "data": {
    // * "lastPrice": 1918, "lastQty": 0.1, "openPrice": 1918, ...... } }
    // */
    // JSONObject jsonMessage = JSONObject.parseObject(str);
    // JSONObject jsonArg = jsonMessage.getJSONObject("arg");
    // String symbol = jsonArg.getString("symbol");
    // String origStreamChannel = jsonArg.getString("channel");
    // StreamChannel streamChannel = StreamChannel.fromCode(origStreamChannel);
    // Object data;
    // if (streamChannel == StreamChannel.KLINE) {
    // data = jsonMessage.getJSONArray("data");
    // } else {
    // data = jsonMessage.getJSONObject("data");
    // }
    // // update local cache
    // updateLocalCache(symbol, streamChannel, origStreamChannel, data);
    // // websocket push
    // StreamRequestArg streamRequestArg = new StreamRequestArg(origStreamChannel, symbol);
    // publicStreamManager.push(streamRequestArg.toTopicString(), str);
    // }
    // }

    /**
     * update local cache
     */
    private void updateLocalCache(String symbol, StreamChannel streamChannel, String origStreamChannel, Object data) {
        switch (streamChannel) {
            case TICKER:
                publicCache.setTicker(symbol, (JSONObject)data);
                break;
            case DEPTH:
                publicCache.setDepth(symbol, (JSONObject)data);
                break;
            case KLINE:
                KlinePeriod period = KlinePeriod.fromChannelName(origStreamChannel);
                publicCache.setKline(symbol, period, (JSONArray)data);
                break;
            case TRADE:
                publicCache.setTrade(symbol, (JSONObject)data);
                break;
            case INDEX_PRICE:
                publicCache.setIndexPrice(symbol, (JSONObject)data);
                break;
            case MARK_PRICE:
                publicCache.setMarkPrice(symbol, (JSONObject)data);
                break;
            case FUNDING_RATE:
                publicCache.setFundingRate(symbol, (JSONObject)data);
                break;
            default:
                break;
        }
    }

}
