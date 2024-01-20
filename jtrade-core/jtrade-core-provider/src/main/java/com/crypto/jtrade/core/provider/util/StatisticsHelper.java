package com.crypto.jtrade.core.provider.util;

import java.util.concurrent.atomic.AtomicBoolean;

import lombok.experimental.UtilityClass;

/**
 * statistics helper
 *
 * @author 0xWill
 **/
@UtilityClass
public class StatisticsHelper {

    private AtomicBoolean statisticsEnabled = new AtomicBoolean(false);

    /**
     * set statistics enabled
     */
    public void setStatisticsEnabled(boolean enabled) {
        statisticsEnabled.set(enabled);
    }

    /**
     * get statistics enabled
     */
    public boolean enabled() {
        return statisticsEnabled.get();
    }

}
