package com.github.xxscloud.demo.lock;



import org.springframework.lang.NonNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Cat.
 */
public final class NodeLock implements NLockCore {


    private static ConcurrentHashMap<String, NodeLockData> NODES = new ConcurrentHashMap<>();
    private static Lock NODE_LOCK = new ReentrantLock();
    private String nodeName;

    private NodeLock(final String nodeName) {
        this.nodeName = nodeName;
    }

    /**
     * 创建一个NodeLock 锁.
     *
     * @param nodeName 锁名称.
     * @return NodeLock 对象.
     */
    public static NodeLock getLock(final String nodeName) {
        return new NodeLock(nodeName);
    }

    private void init(boolean status) {
        if (status) {
            final NodeLockData data = NODES.get(this.nodeName);
            if (data == null) {
                final Lock newLock = new ReentrantLock();
                final AtomicInteger atomicInteger = new AtomicInteger(0);
                NODES.put(this.nodeName, new NodeLockData(newLock, atomicInteger));
            }
        } else {
            if (NODES.get(this.nodeName).getAtomicInteger().addAndGet(-1) <= 0) {
                NODES.remove(this.nodeName);
            }
        }
    }

    private void init() {
        NODE_LOCK.lock();
        try {
            init(true);
            NODES.get(this.nodeName).getAtomicInteger().addAndGet(1);
        } finally {
            NODE_LOCK.unlock();
        }
    }


    @Override
    public void lock() {
        init();
        NODES.get(this.nodeName).getLock().lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        init();
        return NODES.get(this.nodeName).getLock().tryLock();
    }

    @Override
    public boolean tryLock(long time, @NonNull TimeUnit unit) throws InterruptedException {
        init();
        return NODES.get(this.nodeName).getLock().tryLock(time, unit);
    }

    @Override
    public void unlock() {
        NODE_LOCK.lock();
        try {
            if (NODES.get(this.nodeName) == null) {
                return;
            }
            NODES.get(this.nodeName).getLock().unlock();
            init(false);
        } finally {
            NODE_LOCK.unlock();
        }
    }

    @NonNull
    @Override
    public Condition newCondition() {
        return null;
    }


    /**
     * 执行同步方法块.
     *
     * @param nodeName 节点名称.
     * @param action   方法.
     */
    public static void execute(final String nodeName, final NodeLockCallback action) {
        final NodeLock lock = getLock(nodeName);
        lock.lock();
        try {
            action.lockBlock();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 使用tryLock 方式执行同步方法块.
     *
     * @param nodeName 节点名称.
     * @param action   方法.
     */
    public static void executeUseTryLock(final String nodeName, final NodeLockCallback action) {
        final NodeLock lock = getLock(nodeName);
        final boolean status = lock.tryLock();
        if (!status) {
            return;
        }
        try {
            action.lockBlock();
        } finally {
            lock.unlock();
        }
    }
}


