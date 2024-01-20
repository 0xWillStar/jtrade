package com.crypto.jtrade.common.util.concurrent;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.atomic.AtomicBoolean;

import com.crypto.jtrade.common.util.JvmTools;
import com.crypto.jtrade.common.util.StackTraceUtil;
import com.crypto.jtrade.common.util.Utils;

import lombok.extern.slf4j.Slf4j;

/**
 * Fork from sofa-jraft
 *
 * @author 0xWill
 */
@Slf4j
public abstract class AbstractRejectedExecutionHandler implements RejectedExecutionHandler {

    protected final String threadPoolName;

    private final AtomicBoolean dumpNeeded;

    private final String dumpPrefixName;

    public AbstractRejectedExecutionHandler(String threadPoolName, boolean dumpNeeded, String dumpPrefixName) {
        this.threadPoolName = threadPoolName;
        this.dumpNeeded = new AtomicBoolean(dumpNeeded);
        this.dumpPrefixName = dumpPrefixName;
    }

    public void dumpJvmInfoIfNeeded() {
        if (this.dumpNeeded.getAndSet(false)) {
            final String now = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            final String name = this.threadPoolName + "_" + now;
            try (final FileOutputStream fileOutput =
                new FileOutputStream(new File(this.dumpPrefixName + "_dump_" + name + ".log"))) {

                final List<String> stacks = JvmTools.jStack();
                for (final String s : stacks) {
                    fileOutput.write(Utils.getBytes(s));
                }

                final List<String> memoryUsages = JvmTools.memoryUsage();
                for (final String m : memoryUsages) {
                    fileOutput.write(Utils.getBytes(m));
                }
                fileOutput.flush();

                if (JvmTools.memoryUsed() > 0.9) {
                    JvmTools.jMap(this.dumpPrefixName + "_dump_" + name + ".bin", false);
                }
            } catch (final Throwable t) {
                log.error("Dump jvm info error: {}.", StackTraceUtil.stackTrace(t));
            }
        }
    }
}
