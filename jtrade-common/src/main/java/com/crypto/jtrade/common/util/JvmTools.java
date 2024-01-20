package com.crypto.jtrade.common.util;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.crypto.jtrade.common.constants.Constants;
import com.sun.management.HotSpotDiagnosticMXBean;

/**
 * Fork from sofa-jraft
 *
 * @author 0xWill
 */
public final class JvmTools {

    /**
     * Returns java stack traces of java threads for the current java process.
     */
    public static List<String> jStack() throws Exception {
        final List<String> stackList = new LinkedList<>();
        final Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        for (final Map.Entry<Thread, StackTraceElement[]> entry : allStackTraces.entrySet()) {
            final Thread thread = entry.getKey();
            final StackTraceElement[] stackTraces = entry.getValue();

            stackList.add(String.format("\"%s\" tid=%s isDaemon=%s priority=%s" + Constants.NEW_LINE, thread.getName(),
                thread.getId(), thread.isDaemon(), thread.getPriority()));

            stackList.add("java.lang.Thread.State: " + thread.getState() + Constants.NEW_LINE);

            if (stackTraces != null) {
                for (final StackTraceElement s : stackTraces) {
                    stackList.add("    " + s.toString() + Constants.NEW_LINE);
                }
            }
        }
        return stackList;
    }

    /**
     * Returns memory usage for the current java process.
     */
    public static List<String> memoryUsage() throws Exception {
        final MemoryUsage heapMemoryUsage = MXBeanHolder.memoryMxBean.getHeapMemoryUsage();
        final MemoryUsage nonHeapMemoryUsage = MXBeanHolder.memoryMxBean.getNonHeapMemoryUsage();

        final List<String> memoryUsageList = new LinkedList<>();
        memoryUsageList.add(
            "********************************** Memory Usage **********************************" + Constants.NEW_LINE);
        memoryUsageList.add("Heap Memory Usage: " + heapMemoryUsage.toString() + Constants.NEW_LINE);
        memoryUsageList.add("NonHeap Memory Usage: " + nonHeapMemoryUsage.toString() + Constants.NEW_LINE);

        return memoryUsageList;
    }

    /**
     * Returns the heap memory used for the current java process.
     */
    public static double memoryUsed() throws Exception {
        final MemoryUsage heapMemoryUsage = MXBeanHolder.memoryMxBean.getHeapMemoryUsage();
        return (double)(heapMemoryUsage.getUsed()) / heapMemoryUsage.getMax();
    }

    /**
     * Dumps the heap to the outputFile file in the same format as the hprof heap dump.
     *
     * @param outputFile the system-dependent filename
     * @param live if true dump only live objects i.e. objects that are reachable from others
     */
    @SuppressWarnings("all")
    public static void jMap(final String outputFile, final boolean live) throws Exception {
        final File file = new File(outputFile);
        if (file.exists()) {
            file.delete();
        }
        MXBeanHolder.hotSpotDiagnosticMxBean.dumpHeap(outputFile, live);
    }

    private static class MXBeanHolder {

        static final MemoryMXBean memoryMxBean = ManagementFactory.getMemoryMXBean();

        static final HotSpotDiagnosticMXBean hotSpotDiagnosticMxBean =
            ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
    }

    private JvmTools() {}
}
