package io.anyway.bigbang.framework.mutex.service;

import io.anyway.bigbang.framework.mutex.annotation.Mutex;
import io.anyway.bigbang.framework.mutex.dao.MutexMapper;
import io.anyway.bigbang.framework.mutex.domain.MutexState;
import io.anyway.bigbang.framework.mutex.entity.MutexEntity;
import io.anyway.bigbang.framework.discovery.DiscoveryMetadataService;
import io.anyway.bigbang.framework.utils.BeanMapUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

@Aspect
@Slf4j
@Order
public class MutexAspectj {

    private ConcurrentHashMap<String,MutexState> mutexMap= new ConcurrentHashMap<>();

    @Resource
    private MutexMapper mutexMapper;

    @Resource
    private DiscoveryMetadataService discoveryMetadataService;

    @Pointcut("@annotation(io.anyway.bigbang.framework.mutex.annotation.Mutex)")
    public void mutexAspect() {
    }

    @Around("mutexAspect()")
    public Object around(ProceedingJoinPoint jp) throws Throwable {
        Method method= ((MethodSignature)jp.getSignature()).getMethod();
        Mutex mutex= method.getAnnotation(Mutex.class);
        MutexState mutexState= mutexMap.get(mutex.value());
        if(mutexState == null) {
            Calendar calendar= Calendar.getInstance();
            calendar.add(Calendar.SECOND,3 * mutex.heartbeat());
            mutexState = new MutexState()
                    .setServiceId(discoveryMetadataService.getServiceId())
                    .setMutex(mutex.value())
                    .setHost(discoveryMetadataService.getIp() + ":" + discoveryMetadataService.getPort())
                    .setHeartbeat(mutex.heartbeat())
                    .setVersion(UUID.randomUUID().toString())
                    .setDueAt(calendar.getTime())
                    .setRunning(true)
                    .setExecutor(Executors.newFixedThreadPool(1))
                    .setOccupancy(false);
            if(null == mutexMap.putIfAbsent(mutex.value(), mutexState)){
                MutexEntity mutexEntity = BeanMapUtils.map(mutexState, MutexEntity.class);
                int count = mutexMapper.insertMutex(mutexEntity);
                if (count == 1) {
                    mutexState.setOccupancy(true);
                } else {
                    tryMutex(mutexState);
                }
                mutexState.getExecutor().execute(new MutexRunnable(this,mutexState));
            }
        }
        if(mutexState.isOccupancy()){
            return jp.proceed();
        }
        return null;
    }

    private void tryMutex(MutexState mutexState){
        Calendar calendar= Calendar.getInstance();
        calendar.add(Calendar.SECOND,3 * mutexState.getHeartbeat());
        Map<String,Object> inData= new LinkedHashMap<>();
        inData.put("serviceId",mutexState.getServiceId());
        inData.put("mutex",mutexState.getMutex());
        inData.put("host",mutexState.getHost());
        Date dueAt= calendar.getTime();
        inData.put("dueAt",dueAt);
        String nVersion= UUID.randomUUID().toString();
        inData.put("nVersion",nVersion);
        inData.put("oVersion",mutexState.getVersion());
        int count = mutexMapper.updateMutex(inData);
        if(count==1) {
            mutexState.setOccupancy(true);
            mutexState.setVersion(nVersion);
            mutexState.setDueAt(dueAt);
        }
        else{
            mutexState.setOccupancy(false);
        }
    }

    @PreDestroy
    public void destroy(){
        for(MutexState each: mutexMap.values()){
            each.setRunning(false);
            each.setOccupancy(false);
            try {
                each.getExecutor().shutdown();
            }catch (Exception e){
                if(!each.getExecutor().isShutdown()) {
                    each.getExecutor().shutdownNow();
                }
            }
        }
    }

    public static class MutexRunnable implements Runnable{

        final MutexState mutexState;

        final MutexAspectj mutexConfigure;

        public MutexRunnable(MutexAspectj mutexConfigure, MutexState mutexState){
            this.mutexConfigure= mutexConfigure;
            this.mutexState = mutexState;
        }

        @Override
        public void run() {
            for(;mutexState.isRunning();){
                try{
                    long nanosTimeout = TimeUnit.SECONDS.toNanos(mutexState.getHeartbeat());
                    LockSupport.parkNanos(nanosTimeout);
                    mutexConfigure.tryMutex(mutexState);
                }catch (Exception e){
                    log.error("tryMutex error",e);
                }
            }
        }
    }


}
