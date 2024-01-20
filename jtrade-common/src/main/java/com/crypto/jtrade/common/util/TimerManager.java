package com.crypto.jtrade.common.util;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The global timer manager.
 *
 * @author 0xWill
 */
public class TimerManager {

    private static final int WORK_NUMBER = 5;

    private static final String NAME = "jtrade-scheduleThreadPool-";

    private static ScheduledExecutorService executor;
    static {
        executor = ThreadPoolUtil.newScheduledBuilder().poolName(NAME).coreThreads(WORK_NUMBER).enableMetric(true)
            .threadFactory(new NamedThreadFactory(NAME, true)).build();
    }

    public static ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
        return executor.schedule(command, delay, unit);
    }

    public static ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay,
        final long period, final TimeUnit unit) {
        return executor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public static ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay,
        final long delay, final TimeUnit unit) {
        return executor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    public static void shutdown() {
        executor.shutdownNow();
    }

}
