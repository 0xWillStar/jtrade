package com.crypto.jtrade.sinkdb.service.worker.impl;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.crypto.jtrade.common.util.DisruptorBuilder;
import com.crypto.jtrade.common.util.LogExceptionHandler;
import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.sinkdb.model.BatchEntity;
import com.crypto.jtrade.sinkdb.service.rabbitmq.StableClosure;
import com.crypto.jtrade.sinkdb.service.worker.MySqlOperate;
import com.crypto.jtrade.sinkdb.service.worker.MySqlWorker;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * mysql worker
 *
 * @author 0xWill
 **/
@Service
@Slf4j
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class MySqlWorkerImpl implements MySqlWorker {

    private static final Integer CN_RETRY_COUNT = 3;

    @Value("${jtrade.disruptor.worker-buffer-size:8192}")
    private Integer workerBufferSize;

    @Autowired
    private MySqlOperate mySqlOperate;

    private int workerId;

    private long lastBatchId;

    private AtomicBoolean workStopped = new AtomicBoolean(false);

    private Disruptor<BatchEntity> workerDisruptor;

    private RingBuffer<BatchEntity> workerQueue;

    @Override
    public void init(int workerId, long lastBatchId, NamedThreadFactory threadFactory) {
        this.workerId = workerId;
        this.lastBatchId = lastBatchId;

        /**
         * init matchQueue
         */
        this.workerDisruptor = DisruptorBuilder.<BatchEntity>newInstance().setRingBufferSize(workerBufferSize)
            .setEventFactory(new BatchEntityFactory()).setThreadFactory(threadFactory)
            .setProducerType(ProducerType.MULTI).setWaitStrategy(new BlockingWaitStrategy()).build();
        this.workerDisruptor.handleEventsWith(new BatchEntityHandler());
        this.workerDisruptor.setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
        this.workerQueue = this.workerDisruptor.start();
    }

    /**
     * append entity
     */
    @Override
    public void appendEntity(long batchId, List<String> messages, StableClosure stableClosure) {
        if (!workStopped.get()) {
            publishToQueue(batchId, messages, stableClosure);
        }
    }

    /**
     * publish to queue
     */
    private void publishToQueue(long batchId, List<String> messages, StableClosure stableClosure) {
        final EventTranslator<BatchEntity> translator = (event, sequence) -> {
            event.setBatchId(batchId);
            event.setMessageList(messages);
            event.setStableClosure(stableClosure);
        };
        /**
         * FIXME: If the queue is full, publishEvent will be blocking, the system is blocked.
         */
        this.workerQueue.publishEvent(translator);
    }

    /**
     * BatchEntity handler for Disruptor
     */
    private class BatchEntityHandler implements EventHandler<BatchEntity> {

        @Override
        public void onEvent(final BatchEntity batchEntity, final long sequence, final boolean endOfBatch)
            throws Exception {
            if (workStopped.get()) {
                return;
            }

            long batchId = batchEntity.getBatchId();
            if (batchId > lastBatchId) {
                if (!doExecute(batchEntity)) {
                    return;
                }
                lastBatchId = batchId;
            }
            batchEntity.getStableClosure().finishAt(batchId, workerId);
        }
    }

    /**
     * do execute
     */
    private boolean doExecute(BatchEntity batchEntity) {
        for (int i = 1; i <= CN_RETRY_COUNT; i++) {
            try {
                mySqlOperate.batchExecute(workerId, batchEntity.getBatchId(), batchEntity.getMessageList());
                break;
            } catch (Exception e) {
                log.error("batching save data to mysql exception, workerId:{}, retry:{}", workerId, i, e);
                if (i == CN_RETRY_COUNT) {
                    workStopped.set(true);
                    log.error("batching save data to mysql exception, workerId:{}, retry failed, data:{}", workerId,
                        batchEntity.getMessageList());
                    return false;
                }
                ThreadUtil.sleep(RandomUtil.randomInt(10, 100), TimeUnit.MILLISECONDS);
            }
        }
        return true;
    }

    /**
     * BatchEntity factory for Disruptor
     */
    private static class BatchEntityFactory implements EventFactory<BatchEntity> {

        @Override
        public BatchEntity newInstance() {
            return new BatchEntity();
        }
    }

}
