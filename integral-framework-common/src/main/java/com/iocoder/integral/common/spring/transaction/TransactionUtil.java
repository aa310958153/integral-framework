package com.iocoder.integral.common.spring.transaction;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author liqiang
 */
@Slf4j
public class TransactionUtil {

    public static void afterCompletion(Runnable runnable) {
        TransactionUtil.afterCompletion(runnable, null);
    }


    public static void afterCompletion(Runnable runnable, Integer status) {
        TransactionUtil.registerSynchronization(runnable, new TransactionSynchronizationAdapter() {
            @Override
            public void afterCompletion(final int curStatus) {
                if (null == status || status == curStatus) {
                    runnable.run();
                }
            }
        });
    }

    public static void afterCommit(Runnable runnable) {
        TransactionUtil.registerSynchronization(runnable, new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                runnable.run();
            }
        });
    }


    public static void beforeCompletion(Runnable runnable) {
        TransactionUtil.registerSynchronization(runnable, new TransactionSynchronizationAdapter() {
            @Override
            public void beforeCompletion() {
                runnable.run();
            }
        });
    }

    public static void beforeCommit(Runnable runnable) {
        TransactionUtil.beforeCommit(runnable, null);
    }


    public static void beforeCommit(Runnable runnable, Boolean readOnly) {
        TransactionUtil.registerSynchronization(runnable, new TransactionSynchronizationAdapter() {
            @Override
            public void beforeCommit(final boolean curReadOnly) {
                if (null == readOnly || curReadOnly == readOnly) {
                    runnable.run();
                }
            }
        });
    }


    private static void registerSynchronization(Runnable runnable, TransactionSynchronizationAdapter transactionSynchronizationAdapter) {
        // 没事务就直接执行
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.debug("#20 no TransactionSynchronizationManager#isSynchronizationActive, runnable={}", runnable);
            runnable.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(transactionSynchronizationAdapter);
    }
}