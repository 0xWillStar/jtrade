package com.crypto.jtrade.common.util.unsafe;

/**
 * Fork from sofa-jraft
 *
 * @author 0xWill
 */
public interface ReferenceFieldUpdater<U, W> {

    void set(final U obj, final W newValue);

    W get(final U obj);
}
