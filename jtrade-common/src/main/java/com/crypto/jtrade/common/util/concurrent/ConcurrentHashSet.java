package com.crypto.jtrade.common.util.concurrent;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Concurrent hash set.
 * <p>
 * Fork from sofa-jraft
 *
 * @author 0xWill
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> {

    private ConcurrentHashMap<E, Boolean> map;

    /**
     * constructor
     */
    public ConcurrentHashSet() {
        super();
        map = new ConcurrentHashMap<>();
    }

    /**
     * return the size of the map
     *
     * @see java.util.AbstractCollection#size()
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * @see java.util.AbstractCollection#contains(Object)
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    /**
     * @see java.util.AbstractCollection#iterator()
     */
    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    /**
     * add an obj to set, if exist, return false, else return true
     *
     * @see java.util.AbstractCollection#add(Object)
     */
    @Override
    public boolean add(E o) {
        return map.putIfAbsent(o, Boolean.TRUE) == null;
    }

    /**
     * @see java.util.AbstractCollection#remove(Object)
     */
    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    /**
     * clear the set
     *
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * values
     */
    public Collection<E> values() {
        return map.keySet();
    }
}
