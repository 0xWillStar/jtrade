package com.crypto.jtrade.common.util;

import com.crypto.jtrade.common.util.unsafe.ReferenceFieldUpdater;
import com.crypto.jtrade.common.util.unsafe.Updaters;

/**
 * Reuse {@link StringBuilder} based on {@link ThreadLocal}.
 * <p>
 * Be careful that do not to nest in the same thread. Fork from sofa-jraft
 *
 * @author 0xWill
 */
public class StringBuilderHelper {

    private static final ReferenceFieldUpdater<StringBuilder, char[]> valueUpdater =
        Updaters.newReferenceFieldUpdater(StringBuilder.class.getSuperclass(), "value");

    private static final int DISCARD_LIMIT = 1024 << 3; // 8k

    private static final ThreadLocal<StringBuilderHolder> holderThreadLocal =
        ThreadLocal.withInitial(StringBuilderHolder::new);

    public static StringBuilder get() {
        final StringBuilderHolder holder = holderThreadLocal.get();
        return holder.getStringBuilder();
    }

    public static void truncate() {
        final StringBuilderHolder holder = holderThreadLocal.get();
        holder.truncate();
    }

    private static class StringBuilderHolder {

        private final StringBuilder buf = new StringBuilder();

        private StringBuilder getStringBuilder() {
            truncate();
            return buf;
        }

        private void truncate() {
            if (buf.capacity() > DISCARD_LIMIT) {
                valueUpdater.set(buf, new char[1024]);
            }
            buf.setLength(0);
        }
    }
}
