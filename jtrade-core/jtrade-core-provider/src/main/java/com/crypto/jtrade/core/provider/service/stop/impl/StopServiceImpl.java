package com.crypto.jtrade.core.provider.service.stop.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.crypto.jtrade.common.model.BaseResponse;
import com.crypto.jtrade.common.model.Order;
import com.crypto.jtrade.common.util.DisruptorBuilder;
import com.crypto.jtrade.common.util.LogExceptionHandler;
import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.core.provider.model.queue.TriggerPriceEvent;
import com.crypto.jtrade.core.provider.service.cache.LocalCacheService;
import com.crypto.jtrade.core.provider.service.stop.StopService;
import com.crypto.jtrade.core.provider.service.trade.TradeService;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import lombok.extern.slf4j.Slf4j;

/**
 * stop order service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class StopServiceImpl implements StopService {

    @Value("${jtrade.disruptor.stop-buffer-size:2048}")
    private Integer stopBufferSize;

    @Autowired
    private LocalCacheService localCache;

    @Autowired
    private TradeService tradeService;

    private Disruptor<TriggerPriceEvent> stopDisruptor;

    private RingBuffer<TriggerPriceEvent> stopQueue;

    @PostConstruct
    public void init() {
        initStopQueue();
    }

    /**
     * set the trigger price
     */
    @Override
    public void setTriggerPrice(TriggerPriceEvent triggerPriceEvent) {
        publishToQueue(triggerPriceEvent);
    }

    /**
     * publish to queue
     */
    private void publishToQueue(TriggerPriceEvent triggerPriceEvent) {
        final EventTranslator<TriggerPriceEvent> translator = (event, sequence) -> {
            event.setWorkingType(triggerPriceEvent.getWorkingType());
            event.setSymbol(triggerPriceEvent.getSymbol());
            event.setPrice(triggerPriceEvent.getPrice());
        };
        /**
         * FIXME: If the queue is full, publishEvent will be blocking, the system is blocked.
         */
        this.stopQueue.publishEvent(translator);
    }

    /**
     * Stop handler for Disruptor
     */
    private class StopHandler implements EventHandler<TriggerPriceEvent> {

        @Override
        public void onEvent(final TriggerPriceEvent triggerPriceEvent, final long sequence, final boolean endOfBatch)
            throws Exception {
            List<Order> orderList = localCache.getStopOrderCache(triggerPriceEvent.getSymbol())
                .getTriggerOrders(triggerPriceEvent.getWorkingType(), triggerPriceEvent.getPrice());
            if (!CollectionUtils.isEmpty(orderList)) {
                log.info("trigger stop order, triggerPriceEvent: {}, orderList: {}",
                    JSON.toJSONString(triggerPriceEvent), JSON.toJSONString(orderList));
                for (Order order : orderList) {
                    BaseResponse response = tradeService.stopTriggeredPlaceOrder(order);
                    if (response.isError()) {
                        tradeService.stopRejectedCancelOrder(order);
                    }
                }
            }
        }
    }

    /**
     * init stop queue
     */
    private void initStopQueue() {
        this.stopDisruptor = DisruptorBuilder.<TriggerPriceEvent>newInstance().setRingBufferSize(stopBufferSize)
            .setEventFactory(new StopFactory()).setThreadFactory(new NamedThreadFactory("jtrade-stop-disruptor-", true))
            .setProducerType(ProducerType.MULTI).setWaitStrategy(new BlockingWaitStrategy()).build();
        this.stopDisruptor.handleEventsWith(new StopHandler());
        this.stopDisruptor.setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
        this.stopQueue = this.stopDisruptor.start();
    }

    /**
     * stop factory for Disruptor
     */
    private static class StopFactory implements EventFactory<TriggerPriceEvent> {

        @Override
        public TriggerPriceEvent newInstance() {
            return new TriggerPriceEvent();
        }
    }

}
