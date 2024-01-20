package com.crypto.jtrade.core.provider.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

/**
 * client lock helper tool class, a group of clients shares a lock.
 *
 * @author 0xWill
 **/
public class ClientLockHelper {

    private static final Integer LOCKS_COUNT = 16;

    /**
     * KEY： the hashCode of the clientId is mod 16
     */
    private static ConcurrentHashMap<Integer, ReentrantLock> clientLocks = new ConcurrentHashMap<>(LOCKS_COUNT);

    static {
        Stream.iterate(0, i -> i + 1).limit(LOCKS_COUNT).forEach(i -> clientLocks.put(i, new ReentrantLock()));
    }

    /**
     * get the ReentrantLock from cache by clientId
     */
    public static Lock getLock(String clientId) {
        int hashCode = Math.abs(clientId.hashCode());
        return clientLocks.get(hashCode % LOCKS_COUNT);
    }

    /**
     * lock all clients
     */
    public static void lockAll() {
        for (ReentrantLock lock : clientLocks.values()) {
            lock.lock();
        }
    }

    /**
     * unlock all clients
     */
    public static void unlockAll() {
        for (ReentrantLock lock : clientLocks.values()) {
            lock.unlock();
        }
    }

}
