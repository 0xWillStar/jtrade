package com.crypto.jtrade.core.provider.service.trade.impl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.crypto.jtrade.common.constants.Constants;
import com.crypto.jtrade.common.constants.RedisOp;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.exception.TradeException;
import com.crypto.jtrade.common.util.DisruptorBuilder;
import com.crypto.jtrade.common.util.LogExceptionHandler;
import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.common.util.TimerManager;
import com.crypto.jtrade.core.provider.config.TradeLogConfig;
import com.crypto.jtrade.core.provider.model.landing.RedisOperation;
import com.crypto.jtrade.core.provider.model.queue.CommandEvent;
import com.crypto.jtrade.core.provider.model.queue.TradeLogEvent;
import com.crypto.jtrade.core.provider.service.cache.RedisService;
import com.crypto.jtrade.core.provider.service.trade.TradeLog;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * trade log service
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class TradeLogImpl implements TradeLog {

    @Value("${jtrade.disruptor.trade-log-buffer-size:8192}")
    private Integer tradeLogBufferSize;

    @Autowired
    private TradeLogConfig tradeLogConfig;

    @Autowired
    private RedisService redisService;

    private Disruptor<TradeLogEvent> tradeLogDisruptor;

    private RingBuffer<TradeLogEvent> tradeLogQueue;

    private FileChannel currentFileChannel;

    private long currentFileSize;

    private long currentForceSize;

    private List<RedisOperation> currentOperationList;

    private long FILE_MAX_SIZE;

    private long FORCE_MAX_SIZE;

    @PostConstruct
    public void init() {
        FILE_MAX_SIZE = tradeLogConfig.getFileMaxSizeMb() * 1024 * 1024;
        FORCE_MAX_SIZE = tradeLogConfig.getForceMaxSizeKb() * 1024;
        currentFileChannel = createNewLogFile();
        currentOperationList = new ArrayList<>(256);
        currentFileSize = 0;
        currentForceSize = 0;

        initTradeLogQueue();
        initLogForceTimer();
    }

    @Override
    public void publishLog(CommandEvent commandEvent) {
        publishLog(commandEvent, false);
    }

    private void publishLog(CommandEvent commandEvent, boolean timeToForce) {
        final EventTranslator<TradeLogEvent> translator = (event, sequence) -> {
            event.setTimeToForce(timeToForce);
            event.setCommandEvent(commandEvent);
        };
        if (!this.tradeLogQueue.tryPublishEvent(translator)) {
            log.error("System is busy, has too many requests, queue is full and bufferSize={}",
                this.tradeLogQueue.getBufferSize());
            throw new TradeException(TradeError.REQUEST_TOO_MANY);
        }
    }

    /**
     * JCommand handler for Disruptor
     */
    private class JCommandHandler implements EventHandler<TradeLogEvent> {

        @Override
        public void onEvent(final TradeLogEvent logEvent, final long sequence, final boolean endOfBatch)
            throws Exception {
            if (logEvent.isTimeToForce()) {
                forceLogFile();
            } else {
                StringBuilder sb = new StringBuilder(512);
                sb.append(DateUtil.format(DateUtil.date(), "yyyy-MM-dd HH:mm:ss.SSS")).append(" ");
                sb.append(JSON.toJSONString(logEvent.getCommandEvent())).append("\n");
                byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);

                ByteBuffer buffer = ByteBuffer.allocate(data.length);
                buffer.put(data);
                buffer.flip();
                currentFileChannel.write(buffer);

                currentOperationList.add(new RedisOperation(Constants.REDIS_KEY_COMMAND_LOG,
                    Long.toString(logEvent.getCommandEvent().getRequestId()), null, true, RedisOp.HASH));

                currentFileSize += data.length;
                currentForceSize += data.length;
                if (currentForceSize > FORCE_MAX_SIZE) {
                    forceLogFile();
                }
            }
        }
    }

    /**
     * force log file
     */
    private void forceLogFile() {
        try {
            currentFileChannel.force(false);
            redisService.logBatchWriteOperations(currentOperationList);
            currentOperationList.clear();
            currentForceSize = 0;
            if (currentFileSize > FILE_MAX_SIZE) {
                currentFileChannel.close();
                currentFileChannel = createNewLogFile();
                currentFileSize = 0;
            }
        } catch (IOException e) {
            throw new TradeException(e);
        }
    }

    /**
     * init log force timer
     */
    private void initLogForceTimer() {
        long interval = tradeLogConfig.getForceIntervalMilliSeconds();
        TimerManager.scheduleAtFixedRate(() -> onTimeLogForce(), interval, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * time to log force
     */
    private void onTimeLogForce() {
        try {
            publishLog(null, true);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * init the tradeLogQueue
     */
    private void initTradeLogQueue() {
        this.tradeLogDisruptor = DisruptorBuilder.<TradeLogEvent>newInstance().setRingBufferSize(tradeLogBufferSize)
            .setEventFactory(new CommandFactory())
            .setThreadFactory(new NamedThreadFactory("jtrade-trade-log-disruptor-", true))
            .setProducerType(ProducerType.MULTI).setWaitStrategy(new BlockingWaitStrategy()).build();
        this.tradeLogDisruptor.handleEventsWith(new JCommandHandler());
        this.tradeLogDisruptor
            .setDefaultExceptionHandler(new LogExceptionHandler<TradeLogEvent>(getClass().getSimpleName()));
        this.tradeLogQueue = this.tradeLogDisruptor.start();
    }

    /**
     * CommandEvent factory for Disruptor
     */
    private static class CommandFactory implements EventFactory<TradeLogEvent> {

        @Override
        public TradeLogEvent newInstance() {
            return new TradeLogEvent();
        }
    }

    /**
     * create new log file
     */
    private FileChannel createNewLogFile() {
        try {
            FileOutputStream fos = new FileOutputStream(getLogFileName(), true);
            return fos.getChannel();
        } catch (FileNotFoundException e) {
            throw new TradeException(e);
        }
    }

    /**
     * get log file name
     */
    private String getLogFileName() {
        return tradeLogConfig.getFilePath() + "/" + tradeLogConfig.getFilePrefix() + "."
            + DateUtil.format(DateUtil.date(), "yyyyMMddHHmmss") + ".log";
    }

}
