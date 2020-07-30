package io.anyway.bigbang.framework.cache.distributedlock.impl;

import io.anyway.bigbang.framework.cache.CacheService;
import io.anyway.bigbang.framework.cache.service.impl.RedisCacheServiceImpl;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.locks.Lock;

public class DefaultReadLockImpl extends DefaultAbstractLockImpl implements Lock {

    final private static String READ_LOCK_SCRIPT =
            "if (redis.call('hexists',KEYS[1],ARGV[1]) == 0) " +
            "then " +
            "local num= redis.call('hincrby',KEYS[1],ARGV[2],ARGV[3]); " +
            "redis.call('expire',KEYS[1],ARGV[4]); " +
            "return num; " +
            "else " +
            "return 0; " +
            "end";

    final private static String READ_UNLOCK_SCRIPT =
            "local num= redis.call('hincrby',KEYS[1],ARGV[1],ARGV[2]); "+
            "if (num == 0 ) then "+
            "redis.call('del',KEYS[1]); "+
            "end "+
            "return num;";

    final private static String READ_LEASE_SCRIPT =
            "if (redis.call('hexists',KEYS[1],ARGV[1]) == 0) "+
            "then "+
            "redis.call('expire',KEYS[1],ARGV[2]); " +
            "return 1; "+
            "else return 0; end";


    public DefaultReadLockImpl(String name, CacheService cacheService, AsyncTaskExecutor executor){
        super(name,cacheService,executor,"read");
    }

    @Override
    public boolean tryLock2() {
        long result= (Long)((RedisCacheServiceImpl)cacheService).eval(
                READ_LOCK_SCRIPT,
                ReturnType.INTEGER,
                Collections.singletonList(name),
                Arrays.asList("write","read",1,DEFAULT_TIMEOUT));
        return result> 0;
    }

    @Override
    public boolean unlock2() {
        long result= (Long)((RedisCacheServiceImpl)cacheService).eval(
                READ_UNLOCK_SCRIPT,
                ReturnType.INTEGER,
                Collections.singletonList(name),
                Arrays.asList("read",-1));
        return result >=0;
    }

    @Override
    public boolean lease2() {
        long result= (Long)((RedisCacheServiceImpl)cacheService).eval(
                READ_LEASE_SCRIPT,
                ReturnType.INTEGER,
                Collections.singletonList(name),
                Arrays.asList(id,"read",DEFAULT_TIMEOUT));
        return result ==1 ;
    }

}
