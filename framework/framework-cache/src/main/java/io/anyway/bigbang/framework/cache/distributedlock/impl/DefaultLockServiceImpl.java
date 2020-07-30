package io.anyway.bigbang.framework.cache.distributedlock.impl;

import io.anyway.bigbang.framework.cache.CacheService;
import io.anyway.bigbang.framework.cache.distributedlock.LockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

@Slf4j
public class DefaultLockServiceImpl implements LockService {

    @Resource(name="redisCacheService")
    private CacheService cacheService;

    @Resource(name="taskExecutor")
    private AsyncTaskExecutor executor;

    @Override
    public Lock getLock(String name) {
        return new DefaultLockImpl(name, cacheService,executor);
    }

    @Override
    public ReadWriteLock getReadWriteLock(String name) {
        return new DefaultReadWriteLockImpl(name, cacheService,executor);
    }

}
