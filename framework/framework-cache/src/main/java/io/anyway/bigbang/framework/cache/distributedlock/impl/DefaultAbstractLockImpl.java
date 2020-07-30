package io.anyway.bigbang.framework.cache.distributedlock.impl;

import io.anyway.bigbang.framework.cache.CacheService;
import io.anyway.bigbang.framework.cache.exception.MethodNotSupportedException;
import io.anyway.bigbang.framework.cache.distributedlock.LockContext;
import io.anyway.bigbang.framework.cache.distributedlock.LockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;

@Slf4j
public abstract class DefaultAbstractLockImpl implements Lock {

    final protected static long DEFAULT_TIMEOUT= 30*1000;

    final protected String id;

    final protected String name;

    final protected String tag;

    private volatile boolean leaseBool= true;

    final protected static int RETRY_INTERVAL_MILLIS = 100;

    final protected static long RETRY_INTERVAL_NANOS = TimeUnit.MILLISECONDS.toNanos(RETRY_INTERVAL_MILLIS);

    final protected CacheService cacheService;

    final protected AsyncTaskExecutor executor;

    final protected static ThreadLocal<Map<String, LockContext>> LockContextHolder= new ThreadLocal<>();


    public DefaultAbstractLockImpl(String name,CacheService cacheService,AsyncTaskExecutor executor,String tag){
        this.name= "bigbang_distributed_lock_"+name;
        this.cacheService= cacheService;
        this.executor= executor;
        this.tag= tag;
        this.id= UUID.randomUUID().toString();
    }

    @Override
    public void lock() {
        while(!tryLock()) {
            LockSupport.parkNanos(this,RETRY_INTERVAL_NANOS);
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        while(!tryLock()) {
            LockSupport.parkNanos(this,RETRY_INTERVAL_NANOS);
            if (Thread.interrupted()){
                throw new InterruptedException();
            }
        }
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        long waitTime= unit.toMillis(time);
        long alreadyWaitTime = 0;
        while (waitTime > alreadyWaitTime) {
            if (tryLock()){
                return true;
            }
            else{
                if(waitTime-alreadyWaitTime > RETRY_INTERVAL_MILLIS){
                    LockSupport.parkNanos(this,RETRY_INTERVAL_NANOS);
                    if (Thread.interrupted()){
                        throw new InterruptedException();
                    }
                    alreadyWaitTime += RETRY_INTERVAL_MILLIS;
                }
                else{
                    long duration= waitTime- alreadyWaitTime;
                    LockSupport.parkNanos(this,TimeUnit.MILLISECONDS.toNanos(duration));
                    if (Thread.interrupted()){
                        throw new InterruptedException();
                    }
                    return tryLock();
                }
            }
        }
        return false;
    }
    @Override
    public boolean tryLock() {
        //Make sure the re entrance lock be available
        LockContext context;
        Map<String,LockContext> contextMap= LockContextHolder.get();
        if(contextMap!= null
                && (context= contextMap.get(getContextName()))!= null){
            if(id.equals(context.getId())){
                context.increase();
            }
            return true;
        }
        boolean result= tryLock2();
        if(result){
            if(contextMap== null){
                contextMap= new HashMap<>();
                LockContextHolder.set(contextMap);
            }
            contextMap.put(getContextName(),new LockContext(id,1));
            executor.submit(() -> {
                while(leaseBool){
                    LockSupport.parkNanos(this,100 * RETRY_INTERVAL_NANOS);
                    if (Thread.interrupted() || !leaseBool || !lease2()){
                        leaseBool= false;
                    }
                }
            });
        }
        return result;
    }

    public abstract boolean tryLock2();

    public abstract boolean lease2();

    @Override
    public void unlock() {
        LockContext context;
        Map<String,LockContext> contextMap= LockContextHolder.get();
        if(contextMap== null
                || (context= contextMap.get(getContextName()))== null
                || !id.equals(context.getId())
                || context.decrease()>0){
            return;
        }
        contextMap.remove(getContextName());
        if(contextMap.isEmpty()) {
            LockContextHolder.remove();
        }
        boolean result= unlock2();
        if(result){
            leaseBool= false;
        }
        log.info("Thread {} released {} lock, result: {}", getCurrentThread(),name,result);
    }

    public abstract boolean unlock2();

    private String getContextName(){
        return name+"_"+tag;
    }

    @Override
    public Condition newCondition() {
        throw new MethodNotSupportedException("newCondition()");
    }

    protected String getCurrentThread(){
        Thread t = Thread.currentThread();
        String requestId= t.getName()+"_"+t.getId();
        return requestId;
    }
}
