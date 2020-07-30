package io.anyway.bigbang.framework.cache.distributedlock.impl;

import io.anyway.bigbang.framework.cache.CacheService;
import io.anyway.bigbang.framework.cache.service.impl.RedisCacheServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.locks.Lock;

@Slf4j
public class DefaultLockImpl extends DefaultAbstractLockImpl implements Lock {

    final private static String UNLOCK_SCRIPT =
            "local val= redis.call('get',KEYS[1]); "+
            "if (val == ARGV[1] ) then "+
            "redis.call('del',KEYS[1]); "+
            "return 1; "+
            "else return 0; end";

    final private static String LEASE_SCRIPT =
            "if (redis.call('get',KEYS[1]) == ARGV[1] ) then "+
            "redis.call('expire',KEYS[1],ARGV[2]); "+
            "return 1; "+
            "else return 0; end";


    public DefaultLockImpl(String name, CacheService cacheService, AsyncTaskExecutor executor){
        super(name,cacheService,executor,"mutex");
    }

    @Override
    public boolean tryLock2() {
        return cacheService.setNX(name, id,DEFAULT_TIMEOUT);
    }

    @Override
    public boolean unlock2() {
        long result= (Long)((RedisCacheServiceImpl)cacheService).eval(
                UNLOCK_SCRIPT,
                ReturnType.INTEGER,
                Collections.singletonList(name),
                Arrays.asList(id));
        return result ==1 ;
    }

    @Override
    public boolean lease2() {
        long result= (Long)((RedisCacheServiceImpl)cacheService).eval(
                LEASE_SCRIPT,
                ReturnType.INTEGER,
                Collections.singletonList(name),
                Arrays.asList(id,DEFAULT_TIMEOUT));
        return result ==1 ;
    }
}
