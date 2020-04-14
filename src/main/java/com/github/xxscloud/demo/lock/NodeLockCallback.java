package com.github.xxscloud.demo.lock;

import org.springframework.lang.Nullable;

/**
 * @author Cat.
 */
@FunctionalInterface
public interface NodeLockCallback {

    @Nullable
    void lockBlock();
}
