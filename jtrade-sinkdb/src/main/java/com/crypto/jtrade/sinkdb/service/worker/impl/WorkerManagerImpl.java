package com.crypto.jtrade.sinkdb.service.worker.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crypto.jtrade.common.util.NamedThreadFactory;
import com.crypto.jtrade.sinkdb.config.SinkConfig;
import com.crypto.jtrade.sinkdb.mapper.AccomplishMapper;
import com.crypto.jtrade.sinkdb.model.Accomplish;
import com.crypto.jtrade.sinkdb.service.worker.MySqlWorker;
import com.crypto.jtrade.sinkdb.service.worker.WorkerManager;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * worker manager
 *
 * @author 0xWill
 **/
@Service
@Slf4j
public class WorkerManagerImpl implements ApplicationContextAware, WorkerManager {

    @Setter
    private ApplicationContext applicationContext;

    @Autowired
    private SinkConfig sinkConfig;

    @Autowired
    private AccomplishMapper accomplishMapper;

    /**
     * thread factory for disruptor
     */
    private NamedThreadFactory threadFactory = new NamedThreadFactory("MySqlWorker-Disruptor-", true);

    /**
     * KEY：workerId
     */
    private ConcurrentHashMap<Integer, MySqlWorker> workerMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        List<Accomplish> accomplishes = accomplishMapper.getAccomplishes();
        Map<Integer, Accomplish> accomplishMap =
            accomplishes.stream().collect(Collectors.toMap(Accomplish::getWorkerId, accomplish -> accomplish));
        List<Integer> notExistWorkers = new ArrayList<>();
        for (int i = 1; i <= sinkConfig.getWorkerCount(); i++) {
            long batchId = 0;
            Accomplish accomplish = accomplishMap.get(i);
            if (accomplish == null) {
                notExistWorkers.add(i);
            } else {
                batchId = accomplish.getBatchId();
            }

            MySqlWorker worker = applicationContext.getBean(MySqlWorker.class);
            worker.init(i, batchId, threadFactory);
            workerMap.put(i, worker);
        }
        initAccomplishes(notExistWorkers);
    }

    @Transactional
    public void initAccomplishes(List<Integer> notExistWorkers) {
        for (Integer workerId : notExistWorkers) {
            Accomplish accomplish = new Accomplish(workerId, 0L);
            accomplishMapper.addAccomplish(accomplish);
        }
    }

    /**
     * get mysql worker by the clientId
     */
    @Override
    public MySqlWorker getMySqlWorker(String clientId) {
        int hashCode = Math.abs(clientId.hashCode());
        return workerMap.get(hashCode % sinkConfig.getWorkerCount() + 1);
    }

}
