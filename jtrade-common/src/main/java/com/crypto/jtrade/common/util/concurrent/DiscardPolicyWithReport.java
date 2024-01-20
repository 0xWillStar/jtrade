package com.crypto.jtrade.common.util.concurrent;

import java.util.concurrent.ThreadPoolExecutor;

import lombok.extern.slf4j.Slf4j;

/**
 * A handler for rejected tasks that silently discards the rejected task.
 *
 * @author 0xWill
 */
@Slf4j
public class DiscardPolicyWithReport extends AbstractRejectedExecutionHandler {

    public DiscardPolicyWithReport(String threadPoolName) {
        super(threadPoolName, true, "discard");
    }

    @Override
    public void rejectedExecution(final Runnable r, final ThreadPoolExecutor e) {
        log.error("Thread pool [{}] is exhausted! {}.", threadPoolName, e.toString());
        dumpJvmInfoIfNeeded();
    }
}
