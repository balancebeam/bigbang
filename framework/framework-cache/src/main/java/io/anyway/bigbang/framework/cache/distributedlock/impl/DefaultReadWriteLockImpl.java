package io.anyway.bigbang.framework.cache.distributedlock.impl;

import io.anyway.bigbang.framework.cache.CacheService;
import io.anyway.bigbang.framework.cache.distributedlock.LockService;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

public class DefaultReadWriteLockImpl implements ReadWriteLock {

    private String name;

    private CacheService cacheService;

    private AsyncTaskExecutor executor;


    public DefaultReadWriteLockImpl(String name,CacheService cacheService,AsyncTaskExecutor executor){
        this.name= name;
        this.cacheService= cacheService;
        this.executor= executor;
    }

    @Override
    public Lock readLock() {
        return new DefaultReadLockImpl(name,cacheService,executor);
    }

    @Override
    public Lock writeLock() {
        return new DefaultWriteLockImpl(name,cacheService,executor);
    }

}
