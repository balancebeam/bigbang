package io.anyway.bigbang.test.controller;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.cache.CacheService;
import io.anyway.bigbang.framework.cache.distributedlock.LockService;
import io.anyway.bigbang.framework.core.rest.RestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private CacheService cacheService;

    @Resource
    private LockService lockService;

    @RequestMapping(value = "/request", method = RequestMethod.GET)
    public String request(){
        return "request";
    }

    @RequestMapping(value = "/mock", method = RequestMethod.POST)
    public JSONObject mock(@RequestBody JSONObject body){
        return body;
    }

    @RequestMapping(value = "/cache/{k}/{v}", method = RequestMethod.GET)
    public RestBody<Map<String,String>> hello(@PathVariable String k,@PathVariable String v){
        RestBody<Map<String,String>> restBody= new RestBody<>();
        Map<String,String> data= new HashMap<>();
        boolean b= cacheService.setNX(k,v,60*1000);
        data.put("setNX_"+k+"_"+v,String.valueOf(b));
        data.put(k,cacheService.get(k));
        restBody.setData(data);
        return restBody;
    }

    @RequestMapping(value = "/lock/{name}", method = RequestMethod.GET)
    public String lock(@PathVariable String name){
        Lock lock= lockService.getLock("seckill");
        try {
            lock.lock();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("123");
            reentrance(lock);
        }finally {
            lock.unlock();
        }

        return "ok";
    }

    private void reentrance(Lock lock3){
        lock3.lock();
        try {
            Lock lock = lockService.getLock("seckill");
            try {
                lock.lock();
                System.out.println("5678");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Lock newLock = lockService.getLock("order");
                newLock.lock();
                System.out.println("new lock");
                newLock.unlock();
            } finally {
                lock.unlock();
            }
        }
        finally{
            lock3.unlock();
        }
    }
    @RequestMapping(value = "/rlock/{name}", method = RequestMethod.GET)
    public String rlock(@PathVariable String name){
        ReadWriteLock rwLock= lockService.getReadWriteLock(name);
        Lock rlock= rwLock.readLock();
        rlock.lock();
        System.out.println(1);
        rlock.unlock();
        return "ok";
    }

    @RequestMapping(value = "/wlock/{name}", method = RequestMethod.GET)
    public String wlock(@PathVariable String name){
        ReadWriteLock rwLock= lockService.getReadWriteLock(name);
        Lock wlock= rwLock.writeLock();
        wlock.lock();
        System.out.println(1);
        wlock.unlock();
        return "ok";
    }

    @RequestMapping(value = "/rwlock/{name}", method = RequestMethod.GET)
    public String rwlock(@PathVariable String name) {
        ReadWriteLock rwLock = lockService.getReadWriteLock(name);
        Lock rlock = rwLock.readLock();
        rlock.lock();
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(rlock.tryLock()){
                        System.out.println("another read thread");
                    }
                    rlock.unlock();
                }
            });
            t.start();
            Lock wLock = rwLock.writeLock();
            try {
                long begin= System.currentTimeMillis();
                if (wLock.tryLock(5000, TimeUnit.MILLISECONDS)) {
                    System.out.println("get write lock");
                }
                long end= System.currentTimeMillis();
                System.out.println("duration->"+(end-begin));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                wLock.unlock();
            }
        } finally {
            rlock.unlock();
        }
        return "ok";
    }

    @RequestMapping(value = "/wrlock/{name}", method = RequestMethod.GET)
    public String wrlock(@PathVariable String name) {
        ReadWriteLock wrLock = lockService.getReadWriteLock(name);
        Lock wlock = wrLock.writeLock();
        wlock.lock();
        try {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    if(wlock.tryLock()) {
                        System.out.println("another write thread");
                    }
                    wlock.unlock();
                }
            });
            t.start();
            Lock rLock = wrLock.readLock();
            try {
                if (rLock.tryLock(10000, TimeUnit.MILLISECONDS)) {
                    System.out.println("get read lock");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                rLock.unlock();
            }
        } finally {
            wlock.unlock();
        }
        return "ok";
    }

}
