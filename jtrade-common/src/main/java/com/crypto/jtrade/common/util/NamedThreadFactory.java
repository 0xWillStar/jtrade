package com.crypto.jtrade.common.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

/**
 * Named thread factory with prefix.
 *
 * @author 0xWill
 */
@Slf4j
public class NamedThreadFactory implements ThreadFactory {

    private static final LogUncaughtExceptionHandler UNCAUGHT_EX_HANDLER = new LogUncaughtExceptionHandler();

    private final String prefix;

    private final AtomicInteger counter = new AtomicInteger(1);

    private final boolean daemon;

    public NamedThreadFactory(String prefix) {
        this(prefix, false);
    }

    public NamedThreadFactory(String prefix, boolean daemon) {
        super();
        this.prefix = prefix;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setDaemon(this.daemon);
        t.setUncaughtExceptionHandler(UNCAUGHT_EX_HANDLER);
        t.setName(this.prefix + counter.getAndIncrement());
        return t;
    }

    private static final class LogUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            log.error("Uncaught exception in thread {}", t, e);
        }
    }
}
