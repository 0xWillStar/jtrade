package com.crypto.jtrade.core.provider.service.publish.impl;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.constants.CommandIdentity;
import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.OTOOrderType;
import com.crypto.jtrade.common.model.AssetBalance;
import com.crypto.jtrade.common.model.Bill;
import com.crypto.jtrade.common.model.ComplexEntity;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.util.DisruptorBuilder;
import com.crypto.jtrade.common.util.LogExceptionHandler;
import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.common.util.TimerManager;
import com.crypto.jtrade.core.provider.config.CoreConfig;
import com.crypto.jtrade.core.provider.model.landing.FundingFeeLanding;
import com.crypto.jtrade.core.provider.model.queue.PublishEvent;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.landing.RedisLanding;
import com.crypto.jtrade.core.provider.service.oto.OTOService;
import com.crypto.jtrade.core.provider.service.publish.PrivatePublish;
import com.crypto.jtrade.core.provider.service.rabbitmq.BatchingService;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import lombok.extern.slf4j.Slf4j;

/**
 * private data publish service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class PrivatePublishImpl extends BatchingService implements PrivatePublish {

    @Value("${jtrade.disruptor.private-publish-buffer-size:8192}")
    private Integer privatePublishBufferSize;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private RedisLanding redisLanding;

    @Autowired
    private OTOService otoService;

    private CoreConfig coreConfig;

    private Long rabbitBatchId;

    private Disruptor<PublishEvent> privatePublishDisruptor;

    private RingBuffer<PublishEvent> privatePublishQueue;

    @Autowired
    public PrivatePublishImpl(RabbitTemplate rabbitTemplate, CoreConfig coreConfig) {
        super(rabbitTemplate, coreConfig.getPublicPublishMaxBatchSize(), Constants.MQ_EXCHANGE_STREAM_PRIVATE, null);
        this.coreConfig = coreConfig;
    }

    @PostConstruct
    public void init() {
        initPrivatePublishQueue();
        initPrivatePublishTimer();
    }

    /**
     * publish complex entity
     */
    @Override
    public void publishComplex(ComplexEntity complexEntity) {
        publishToQueue(CommandIdentity.PUBLISH_COMPLEX, complexEntity);
    }

    /**
     * set funding fee
     */
    @Override
    public void setFundingFee(FundingFeeLanding fundingFeeLanding) {
        publishToQueue(CommandIdentity.SET_FUNDING_FEE, fundingFeeLanding);
    }

    /**
     * get the batch id
     */
    @Override
    protected Long getRabbitBatchId() {
        return 0L;
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
        this.privatePublishQueue.publishEvent(translator);
    }

    /**
     * PublishEvent handler for Disruptor
     */
    private class PrivatePublishHandler implements EventHandler<PublishEvent> {

        @Override
        public void onEvent(final PublishEvent publishEvent, final long sequence, final boolean endOfBatch)
            throws Exception {
            switch (publishEvent.getIdentity()) {
                case PUBLISH_COMPLEX:
                    publishComplexHandler((ComplexEntity)publishEvent.getData());
                    break;
                case SET_FUNDING_FEE:
                    publishFundingFeeHandler((FundingFeeLanding)publishEvent.getData());
                    break;
                case PRIVATE_PUBLISH:
                    privatePublishHandler();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * publish complex handler
     */
    private void publishComplexHandler(ComplexEntity complexEntity) {
        addToBatch(complexEntity.toJSONString(), true);
        Order order = complexEntity.getOrder();
        if (order != null && order.getOtoOrderType() != OTOOrderType.NONE) {
            otoService.receiveOTOEvent(complexEntity);
        }
    }

    /**
     * publish funding fee handler
     */
    private void publishFundingFeeHandler(FundingFeeLanding fundingFeeLanding) {
        for (AssetBalance balance : fundingFeeLanding.getBalances()) {
            ComplexEntity complexEntity = new ComplexEntity(null, Collections.singletonList(balance), null, null, null);
            addToBatch(complexEntity.toJSONString(), true);
        }
        for (Bill bill : fundingFeeLanding.getBills()) {
            ComplexEntity complexEntity = new ComplexEntity(null, null, null, null, Collections.singletonList(bill));
            addToBatch(complexEntity.toJSONString(), true);
        }
    }

    /**
     * PRIVATE_PUBLISH handler
     */
    private void privatePublishHandler() {
        sendData();
    }

    /**
     * init private publish timer
     */
    private void initPrivatePublishTimer() {
        int interval = coreConfig.getPrivatePublishIntervalMilliSeconds();
        TimerManager.scheduleAtFixedRate(() -> onTimePrivatePublish(), interval, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * time to private publish
     */
    private void onTimePrivatePublish() {
        try {
            publishToQueue(CommandIdentity.PRIVATE_PUBLISH, null);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * init private publish queue
     */
    private void initPrivatePublishQueue() {
        this.privatePublishDisruptor = DisruptorBuilder.<PublishEvent>newInstance()
            .setRingBufferSize(privatePublishBufferSize).setEventFactory(new PrivatePublishFactory())
            .setThreadFactory(new NamedThreadFactory("jtrade-privatePublish-disruptor-", true))
            .setProducerType(ProducerType.MULTI).setWaitStrategy(new BlockingWaitStrategy()).build();
        this.privatePublishDisruptor.handleEventsWith(new PrivatePublishHandler());
        this.privatePublishDisruptor
            .setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
        this.privatePublishQueue = this.privatePublishDisruptor.start();
    }

    /**
     * PublishEvent factory for Disruptor
     */
    private static class PrivatePublishFactory implements EventFactory<PublishEvent> {

        @Override
        public PublishEvent newInstance() {
            return new PublishEvent<>();
        }
    }

}
