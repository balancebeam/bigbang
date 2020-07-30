package io.anyway.bigbang.framework.cache.distributedlock.impl;

import io.anyway.bigbang.framework.cache.CacheService;
import io.anyway.bigbang.framework.cache.service.impl.RedisCacheServiceImpl;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;

public class DefaultWriteLockImpl extends DefaultAbstractLockImpl implements Lock {

    final private static String WRITE_LOCK_SCRIPT =
            "local num= redis.call('hexists',KEYS[1],ARGV[1]); "+
            "if (num > 0) then return 0; "+
            "else local result= redis.call('hsetnx',KEYS[1],ARGV[2],ARGV[3]); "+
            "if (result == 1) then redis.call('expire',KEYS[1],ARGV[4]); "+
            "end return result; end";

    final private static String WRITE_UNLOCK_SCRIPT =
            "if(redis.call('hget',KEYS[1],ARGV[1]) == ARGV[2]) "+
            "then return redis.call('del',KEYS[1]); "+
            "else return 0; end";

    final private static String READ_LEASE_SCRIPT =
            "if (redis.call('hget',KEYS[1],ARGV[1]) == ARGV[2]) "+
            "then "+
            "redis.call('expire',KEYS[1],ARGV[3]); " +
            "return 1; "+
            "else return 0; end";

    public DefaultWriteLockImpl(String name, CacheService cacheService, AsyncTaskExecutor executor){
        super(name,cacheService,executor,"write");
    }

    @Override
    public boolean tryLock2() {
        long result= (Long)((RedisCacheServiceImpl)cacheService).eval(
                WRITE_LOCK_SCRIPT,
                ReturnType.INTEGER,
                Collections.singletonList(name),
                Arrays.asList("read","write",id,DEFAULT_TIMEOUT));
        return result> 0;
    }

    @Override
    public boolean unlock2() {
        long result= (Long)((RedisCacheServiceImpl)cacheService).eval(
                WRITE_UNLOCK_SCRIPT,
                ReturnType.INTEGER,
                Collections.singletonList(name),
                Arrays.asList("write",id));
        return result> 0;
    }

    @Override
    public boolean lease2() {
        long result= (Long)((RedisCacheServiceImpl)cacheService).eval(
                READ_LEASE_SCRIPT,
                ReturnType.INTEGER,
                Collections.singletonList(name),
                Arrays.asList("write",id,DEFAULT_TIMEOUT));
        return result ==1 ;
    }
}
