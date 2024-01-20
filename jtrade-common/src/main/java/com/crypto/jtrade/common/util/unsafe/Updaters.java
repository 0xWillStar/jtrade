package com.crypto.jtrade.common.util.unsafe;

/**
 * Sometime instead of reflection, better performance.
 * <p>
 * Fork from sofa-jraft
 *
 * @author 0xWill
 */
public class Updaters {

    /**
     * Creates and returns an updater for objects with the given field.
     *
     * @param tClass the class of the objects holding the field.
     * @param fieldName the name of the field to be updated.
     */
    public static <U, W> ReferenceFieldUpdater<U, W> newReferenceFieldUpdater(final Class<? super U> tClass,
        final String fieldName) {
        try {
            if (UnsafeUtil.hasUnsafe()) {
                return new UnsafeReferenceFieldUpdater<>(UnsafeUtil.getUnsafeAccessor().getUnsafe(), tClass, fieldName);
            } else {
                return new ReflectionReferenceFieldUpdater<>(tClass, fieldName);
            }
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
