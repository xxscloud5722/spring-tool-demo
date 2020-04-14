package com.github.xxscloud.demo.spring;


import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

/**
 * @author Cat.
 * 手动事务.
 */
public final class TransactionCore {

    private final TransactionStatus transactionStatus;
    private PlatformTransactionManager platformTransactionManager;


    private TransactionCore() {
        this.transactionStatus = null;
    }

    private TransactionCore(final PlatformTransactionManager platformTransactionManager, final TransactionStatus transactionStatus) {
        this.platformTransactionManager = platformTransactionManager;
        this.transactionStatus = transactionStatus;
    }

    /**
     * 开启默认配置手动事务.
     *
     * @return 事务对象.
     */
    public static TransactionCore getDefaultTransaction() {
        return getDefaultTransaction(1500, TransactionDefinition.PROPAGATION_REQUIRED);
    }

    /**
     * 开启默认配置手动事务.
     *
     * @param time 超时时间.
     * @return 事务对象.
     */
    public static TransactionCore getDefaultTransaction(final int time) {
        return getDefaultTransaction(time, TransactionDefinition.PROPAGATION_REQUIRED);
    }

    /**
     * 开启默认配置手动事务.
     *
     * @param time                  超时毫秒.
     * @param transactionDefinition 事务类型.
     * @return 事务对象.
     */
    public static TransactionCore getDefaultTransaction(final int time, final int transactionDefinition) {
        final AbstractPlatformTransactionManager abstractPlatformTransactionManager = SpringContextUtils.getBean(AbstractPlatformTransactionManager.class);
        if (abstractPlatformTransactionManager == null) {
            throw new RuntimeException(PlatformTransactionManager.class.getName() + "IS NULL");
        }

        final DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        defaultTransactionDefinition.setPropagationBehavior(transactionDefinition);
        defaultTransactionDefinition.setTimeout(time);
        if (Objects.equals(transactionDefinition, TransactionDefinition.PROPAGATION_NOT_SUPPORTED)) {
            return new TransactionCore(abstractPlatformTransactionManager, abstractPlatformTransactionManager.getTransaction(defaultTransactionDefinition));
        }
        final TransactionStatus transactionStatus = abstractPlatformTransactionManager.getTransaction(defaultTransactionDefinition);
        if (transactionStatus.isNewTransaction()) {
            return new TransactionCore(abstractPlatformTransactionManager, transactionStatus);
        }
        return new TransactionCore();
    }

    public void commit() {
        if (transactionStatus == null) {
            return;
        }
        if (transactionStatus.isCompleted()) {
            return;
        }
        platformTransactionManager.commit(transactionStatus);
    }

    public void rollback() {
        if (transactionStatus == null) {
            return;
        }
        if (transactionStatus.isCompleted()) {
            return;
        }
        platformTransactionManager.rollback(transactionStatus);
    }

    /**
     * 执行事务方法块.
     *
     * @param action 方法.
     */
    public static <T> void execute(final TransactionCallback<T> action) {
        final TransactionTemplate transactionTemplate = SpringContextUtils.getBean(TransactionTemplate.class);
        if (transactionTemplate == null) {
            throw new RuntimeException("bean is null");
        }
        transactionTemplate.execute(action);
    }
}
