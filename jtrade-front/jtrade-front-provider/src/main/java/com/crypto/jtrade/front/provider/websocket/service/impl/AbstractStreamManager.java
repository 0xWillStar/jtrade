package com.crypto.jtrade.front.provider.websocket.service.impl;

import static com.crypto.jtrade.common.constants.Constants.COLON_STR;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.crypto.jtrade.common.constants.StreamChannel;
import com.crypto.jtrade.common.exception.TradeError;
import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.common.util.TimerManager;
import com.crypto.jtrade.front.provider.cache.PublicCache;
import com.crypto.jtrade.front.provider.config.FrontConfig;
import com.crypto.jtrade.front.provider.constants.Constants;
import com.crypto.jtrade.front.provider.constants.StreamOp;
import com.crypto.jtrade.front.provider.model.StreamRequest;
import com.crypto.jtrade.front.provider.model.StreamRequestArg;
import com.crypto.jtrade.front.provider.model.StreamResponse;
import com.crypto.jtrade.front.provider.service.ApiKeyService;
import com.crypto.jtrade.front.provider.websocket.handler.HeadHandler;
import com.crypto.jtrade.front.provider.websocket.service.StreamManager;
import com.crypto.jtrade.front.provider.websocket.service.StreamWorker;

import cn.hutool.core.map.MapUtil;
import io.netty.channel.Channel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * base stream manager
 *
 * @author 0xWill
 **/
@Slf4j
public abstract class AbstractStreamManager implements ApplicationContextAware, StreamManager {

    @Setter
    private ApplicationContext applicationContext;

    @Autowired
    private FrontConfig frontConfig;

    @Autowired
    private PublicCache publicCache;

    @Autowired
    private ApiKeyService apiKeyService;

    /**
     * KEY: workerId
     */
    protected final ConcurrentHashMap<Integer, StreamWorker> streamWorkerMap = new ConcurrentHashMap<>();

    /**
     * init
     */
    @PostConstruct
    public void init() {
        NamedThreadFactory threadFactory = new NamedThreadFactory(getThreadFactoryPrefix(), true);
        for (int i = 1; i <= getStreamWorkerSize(); i++) {
            StreamWorker streamWorker = applicationContext.getBean(StreamWorker.class);
            streamWorker.init(i, threadFactory);
            streamWorkerMap.put(i, streamWorker);
        }
        initPingTimer();
        initFlushTimer();
    }

    /**
     * add new channel
     */
    @Override
    public void addChannel(String sessionId, Channel channel) {
        StreamWorker streamWorker = getStreamWorker(sessionId);
        if (streamWorker != null) {
            streamWorker.add(sessionId, channel);
        }
    }

    /**
     * remove channel
     */
    @Override
    public void removeChannel(String sessionId) {
        StreamWorker streamWorker = getStreamWorker(sessionId);
        if (streamWorker != null) {
            streamWorker.remove(sessionId);
        }
    }

    /**
     * websocket client request
     */
    @Override
    public void request(String sessionId, Channel channel, String message) {
        StreamWorker streamWorker = getStreamWorker(sessionId);
        if (streamWorker == null) {
            return;
        }

        StreamResponse response = new StreamResponse();
        if (StringUtils.isEmpty(message)) {
            response.setError(TradeError.ARGUMENT_INVALID);
            streamWorker.response(sessionId, JSON.toJSONString(response));
        } else if (message.equals(Constants.PONG)) {
            streamWorker.pong(sessionId);
        } else {
            if (!JSONObject.isValid(message)) {
                response.setError(TradeError.ARGUMENT_INVALID);
                streamWorker.response(sessionId, JSON.toJSONString(response));
                return;
            }

            StreamRequest request = JSONObject.parseObject(message, new TypeReference<StreamRequest>() {});
            response.setEvent(request.getOp());
            if (request.getOp() == null || CollectionUtils.isEmpty(request.getArgs())) {
                response.setError(TradeError.ARGUMENT_INVALID);

            } else {
                if (request.getOp() == StreamOp.auth) {
                    String clientId = null;
                    try {
                        clientId = checkAuth(sessionId, channel, request.getArgs().get(0));
                    } catch (Exception e) {
                        response.setError(TradeError.ARGUMENT_INVALID);
                    }
                    if (clientId != null) {
                        adjustWorker(sessionId, channel, clientId, streamWorker.getWorkerId());
                    }

                } else {
                    for (StreamRequestArg argument : request.getArgs()) {
                        response.setArg(argument);
                        StreamChannel reqChannel = StreamChannel.fromCode(argument.getChannel());
                        if (reqChannel == null) {
                            response.setError(TradeError.CHANNEL_NOT_EXIST);
                        } else if (reqChannel.isSymbolAllowed()
                            && publicCache.getSymbolInfo(argument.getSymbol()) == null) {
                            response.setError(TradeError.SYMBOL_NOT_EXIST);
                        } else {
                            String topic = getTopicString(sessionId, argument);
                            if (topic == null) {
                                response.setError(TradeError.REQUEST_ILLEGAL);
                            } else {
                                switch (request.getOp()) {
                                    case subscribe:
                                        streamWorker.subscribe(sessionId, topic);
                                        response.setError(TradeError.SUCCESS);
                                        break;
                                    case unsubscribe:
                                        streamWorker.unsubscribe(sessionId, topic);
                                        response.setError(TradeError.SUCCESS);
                                        break;
                                    default:
                                        response.setError(TradeError.ARGUMENT_INVALID);
                                        break;
                                }
                            }
                        }
                    }
                }
            }
            streamWorker.response(sessionId, JSON.toJSONString(response));
        }
    }

    /**
     * is private
     */
    protected abstract boolean isPrivateStreamManager();

    /**
     * get size of stream worker
     */
    protected abstract Integer getStreamWorkerSize();

    /**
     * get prefix of the thread factory
     */
    protected abstract String getThreadFactoryPrefix();

    /**
     * get topic string
     */
    protected abstract String getTopicString(String sessionId, StreamRequestArg argument);

    /**
     * get worker id
     */
    protected abstract Integer getWorkerId(String sessionId);

    /**
     * adjust worker
     */
    protected void adjustWorker(String sessionId, Channel channel, String clientId, int workerId) {

    }

    /**
     * push data to worker
     */
    protected void pushToWorker(Integer workerId, String topic, String data) {
        StreamWorker worker = streamWorkerMap.get(workerId);
        worker.push(topic, data);
    }

    /**
     * push data to all workers
     */
    protected void pushToAllWorkers(String topic, String data) {
        for (StreamWorker worker : streamWorkerMap.values()) {
            worker.push(topic, data);
        }
    }

    /**
     * init ping timer
     */
    private void initPingTimer() {
        long currTime = System.currentTimeMillis();
        int interval = frontConfig.getPingIntervalSeconds() * 1000;
        long delay = (currTime / interval + 1) * interval - currTime;
        TimerManager.scheduleAtFixedRate(() -> onTimePing(), delay, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * init flush timer
     */
    private void initFlushTimer() {
        long currTime = System.currentTimeMillis();
        int interval = frontConfig.getFlushIntervalMilliSeconds();
        long delay = (currTime / interval + 1) * interval - currTime;
        TimerManager.scheduleAtFixedRate(() -> onTimeFlush(), delay, interval, TimeUnit.MILLISECONDS);
    }

    /**
     * time to ping
     */
    private void onTimePing() {
        try {
            for (StreamWorker worker : streamWorkerMap.values()) {
                worker.ping();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * time to flush
     */
    private void onTimeFlush() {
        try {
            for (StreamWorker worker : streamWorkerMap.values()) {
                worker.flush();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * get stream worker by session id
     */
    private StreamWorker getStreamWorker(String sessionId) {
        return streamWorkerMap.get(getWorkerId(sessionId));
    }

    /**
     * check authority
     */
    private String checkAuth(String sessionId, Channel channel, StreamRequestArg argument) {
        String host = getHost(sessionId, channel);
        String uri = "/users/self/verify";
        String method = "GET";
        return apiKeyService.checkSignature(argument.getApiKey(), host, uri, method, argument.getTimestamp(),
            MapUtil.newHashMap(), argument.getSignature());
    }

    /**
     * get host
     */
    private String getHost(String sessionId, Channel channel) {
        String host = HeadHandler.SESSION_HOST_MAP.get(sessionId);
        if (StringUtils.isBlank(host)) {
            host = ((InetSocketAddress)channel.remoteAddress()).getHostString();
        }
        if (host.contains(COLON_STR)) {
            host = host.substring(0, host.indexOf(COLON_STR));
        }
        return host;
    }

}
