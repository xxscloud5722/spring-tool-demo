package com.github.xxscloud.demo.lock;

import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

/**
 * @author Cat.
 */
@Data
public final class NodeLockData {
    private Lock lock;
    private AtomicInteger atomicInteger;

    NodeLockData(Lock lock, AtomicInteger atomicInteger) {
        this.lock = lock;
        this.atomicInteger = atomicInteger;
    }
}
