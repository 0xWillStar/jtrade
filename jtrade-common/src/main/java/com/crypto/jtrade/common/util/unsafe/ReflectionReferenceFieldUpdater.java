package com.crypto.jtrade.common.util.unsafe;

import java.lang.reflect.Field;

/**
 * Fork from sofa-jraft
 *
 * @author 0xWill
 */
final class ReflectionReferenceFieldUpdater<U, W> implements ReferenceFieldUpdater<U, W> {

    private final Field field;

    ReflectionReferenceFieldUpdater(Class<? super U> tClass, String fieldName) throws NoSuchFieldException {
        this.field = tClass.getDeclaredField(fieldName);
        this.field.setAccessible(true);
    }

    @Override
    public void set(final U obj, final W newValue) {
        try {
            this.field.set(obj, newValue);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public W get(final U obj) {
        try {
            return (W)this.field.get(obj);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
