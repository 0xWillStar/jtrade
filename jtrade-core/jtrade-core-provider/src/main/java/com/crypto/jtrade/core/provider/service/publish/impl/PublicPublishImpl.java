package com.crypto.jtrade.core.provider.service.publish.impl;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.CommandIdentity;
import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.MatchRole;
import com.crypto.jtrade.common.constants.StreamChannel;
import com.crypto.jtrade.common.constants.WorkingType;
import com.crypto.jtrade.common.model.Depth;
import com.crypto.jtrade.common.model.FundingRate;
import com.crypto.jtrade.common.model.MarkPrice;
import com.crypto.jtrade.common.model.StreamArgument;
import com.crypto.jtrade.common.model.Trade;
import com.crypto.jtrade.common.util.DisruptorBuilder;
import com.crypto.jtrade.common.util.LogExceptionHandler;
import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.common.util.StreamUtils;
import com.crypto.jtrade.common.util.TimerManager;
import com.crypto.jtrade.core.provider.config.CoreConfig;
import com.crypto.jtrade.core.provider.model.queue.PublishEvent;
import com.crypto.jtrade.core.provider.model.queue.TriggerPriceEvent;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.landing.RedisLanding;
import com.crypto.jtrade.core.provider.service.publish.MarketService;
import com.crypto.jtrade.core.provider.service.publish.PublicPublish;
import com.crypto.jtrade.core.provider.service.rabbitmq.BatchingService;
import com.crypto.jtrade.core.provider.service.rabbitmq.MessageClosure;
import com.crypto.jtrade.core.provider.service.stop.StopService;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import lombok.extern.slf4j.Slf4j;

/**
 * public data publish service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class PublicPublishImpl extends BatchingService implements PublicPublish {

    @Value("${jtrade.disruptor.public-publish-buffer-size:8192}")
    private Integer publicPublishBufferSize;

    private CoreConfig coreConfig;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private RedisLanding redisLanding;

    @Autowired
    private MarketService marketService;

    @Autowired
    private StopService stopService;

    private Disruptor<PublishEvent> publicPublishDisruptor;

    private RingBuffer<PublishEvent> publicPublishQueue;

    @Autowired
    public PublicPublishImpl(RabbitTemplate rabbitTemplate, CoreConfig coreConfig) {
        super(rabbitTemplate, coreConfig.getPublicPublishMaxBatchSize(), Constants.MQ_EXCHANGE_STREAM_PUBLIC, null);
        this.coreConfig = coreConfig;
    }

    @PostConstruct
    public void init() {
        marketService.setMessageClosure(new PublicPublishMessageClosure());
        initPublicPublishQueue();
        initPublicPublishTimer();
    }

    /**
     * publish depth
     */
    @Override
    public void publishDepth(Depth depth) {
        publishToQueue(CommandIdentity.PUBLISH_DEPTH, depth);
    }

    /**
     * publish trade
     */
    @Override
    public void publishTrade(Trade trade) {
        publishToQueue(CommandIdentity.PUBLISH_TRADE, trade);
    }

    /**
     * publish mark price
     */
    @Override
    public void publishMarkPrice(MarkPrice markPrice) {
        publishToQueue(CommandIdentity.PUBLISH_MARK_PRICE, markPrice);
    }

    /**
     * publish funding rate
     */
    @Override
    public void publishFundingRate(FundingRate fundingRate) {
        publishToQueue(CommandIdentity.PUBLISH_FUNDING_RATE, fundingRate);
    }

    /**
     * publish to queue
     */
    private void publishToQueue(CommandIdentity identity, Object data) {
        final EventTranslator<PublishEvent> translator = (event, sequence) -> {
            event.setIdentity(identity);
            event.setData(data);
        };
        /**
         * FIXME: If the queue is full, publishEvent will be blocking, the system is blocked.
         */
        if (this.publicPublishQueue != null) {
            this.publicPublishQueue.publishEvent(translator);
        }
    }

    /**
     * get the batch id
     */
    @Override
    protected Long getRabbitBatchId() {
        return 0L;
    }

    /**
     * PublishEvent handler for Disruptor
     */
    private class PublicPublishHandler implements EventHandler<PublishEvent> {

        @Override
        public void onEvent(final PublishEvent publishEvent, final long sequence, final boolean endOfBatch)
            throws Exception {
            switch (publishEvent.getIdentity()) {
                case PUBLISH_DEPTH:
                    publishDepthHandler((Depth)publishEvent.getData());
                    break;
                case PUBLISH_TRADE:
                    publishTradeHandler((Trade)publishEvent.getData());
                    break;
                case PUBLISH_MARK_PRICE:
                    publishMarkPriceHandler((MarkPrice)publishEvent.getData());
                    break;
                case PUBLISH_FUNDING_RATE:
                    publishFundingRateHandler((FundingRate)publishEvent.getData());
                    break;
                case PUBLISH_RAW:
                    addToBatch((String)publishEvent.getData(), false);
                    break;
                case PUBLIC_PUBLISH:
                    publicPublishHandler();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * publish depth handler
     */
    private void publishDepthHandler(Depth depth) {
        StreamArgument argument = new StreamArgument(StreamChannel.DEPTH.getCode(), depth.getSymbol());
        String content = StreamUtils.getJSONString(argument.toJSONString(), depth.toJSONString());
        addToBatch(content, false);
        /**
         * Pushing the depth from the matching engine is not real-time, but at intervals, so real-time push is required
         * here.
         */
        sendData();
    }

    /**
     * publish trade handler
     */
    private void publishTradeHandler(Trade trade) {
        if (trade.getMatchRole() == MatchRole.TAKER) {
            stopService
                .setTriggerPrice(new TriggerPriceEvent(WorkingType.LAST_PRICE, trade.getSymbol(), trade.getPrice()));
            marketService.tradeHandler(trade);
        }
    }

    /**
     * publish mark price handler
     */
    private void publishMarkPriceHandler(MarkPrice markPrice) {
        stopService.setTriggerPrice(
            new TriggerPriceEvent(WorkingType.MARK_PRICE, markPrice.getSymbol(), markPrice.getMarkPrice()));
        // public publish
        StreamArgument argument = new StreamArgument(StreamChannel.MARK_PRICE.getCode(), markPrice.getSymbol());
        String content = StreamUtils.getJSONString(argument.toJSONString(), markPrice.toJSONString());
        addToBatch(content, true);
    }

    /**
     * publish funding rate handler
     */
    private void publishFundingRateHandler(FundingRate fundingRate) {
        StreamArgument argument = new StreamArgument(StreamChannel.FUNDING_RATE.getCode(), fundingRate.getSymbol());
        String content = StreamUtils.getJSONString(argument.toJSONString(), fundingRate.toJSONString());
        addToBatch(content, true);
    }

    /**
     * PUBLIC_PUBLISH handler
     */
    private void publicPublishHandler() {
        sendData();
    }

    /**
     * init public publish timer
     */
    private void initPublicPublishTimer() {
        int interval = coreConfig.getPublicPublishIntervalMilliSeconds();
        TimerManager.scheduleAtFixedRate(() -> onTimePublicPublish(), interval, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * time to public publish
     */
    private void onTimePublicPublish() {
        try {
            publishToQueue(CommandIdentity.PUBLIC_PUBLISH, null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * init public publish queue
     */
    private void initPublicPublishQueue() {
        this.publicPublishDisruptor = DisruptorBuilder.<PublishEvent>newInstance()
            .setRingBufferSize(publicPublishBufferSize).setEventFactory(new PublicPublishFactory())
            .setThreadFactory(new NamedThreadFactory("jtrade-publicPublish-disruptor-", true))
            .setProducerType(ProducerType.MULTI).setWaitStrategy(new BlockingWaitStrategy()).build();
        this.publicPublishDisruptor.handleEventsWith(new PublicPublishHandler());
        this.publicPublishDisruptor
            .setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
        this.publicPublishQueue = this.publicPublishDisruptor.start();
    }

    /**
     * PublishEvent factory for Disruptor
     */
    private static class PublicPublishFactory implements EventFactory<PublishEvent> {

        @Override
        public PublishEvent newInstance() {
            return new PublishEvent<>();
        }
    }

    class PublicPublishMessageClosure implements MessageClosure {

        public PublicPublishMessageClosure() {

        }

        @Override
        public void addToBatch(String data, boolean trySend) {
            PublicPublishImpl.this.addToBatch(data, trySend);
        }

        @Override
        public void publishToQueue(CommandIdentity identity, Object data) {
            PublicPublishImpl.this.publishToQueue(identity, data);
        }
    }

}
