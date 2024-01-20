package com.crypto.jtrade.common.util;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

/**
 * A {@link java.util.concurrent.ExecutorService} that witch can print error message for failed execution.
 *
 * @author 0xWill
 */
@Slf4j
public class LogThreadPoolExecutor extends ThreadPoolExecutor {

    private final String name;

    public LogThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue, String name) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        this.name = name;
    }

    public LogThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, String name) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        this.name = name;
    }

    public LogThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler, String name) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
        this.name = name;
    }

    public LogThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
        BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler, String name) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
                final Future<?> f = (Future<?>)r;
                if (f.isDone()) {
                    f.get();
                }
            } catch (final CancellationException ce) {
                // ignored
            } catch (final ExecutionException ee) {
                t = ee.getCause();
            } catch (final InterruptedException ie) {
                Thread.currentThread().interrupt(); // ignore/reset
            }
        }
        if (t != null) {
            log.error("Uncaught exception in pool: {}, {}.", this.name, super.toString(), t);
        }
    }

    @Override
    protected void terminated() {
        super.terminated();
        log.info("ThreadPool is terminated: {}, {}.", this.name, super.toString());
    }
}
