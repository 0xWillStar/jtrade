package com.crypto.jtrade.common.util.recycle;

/**
 * Recycle tool for {@link Recyclable}.
 *
 * @author 0xWill
 */
public final class RecycleUtil {

    /**
     * Recycle designated instance.
     */
    public static boolean recycle(final Object obj) {
        return obj instanceof Recyclable && ((Recyclable)obj).recycle();
    }

    private RecycleUtil() {}
}
