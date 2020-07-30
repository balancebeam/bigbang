package io.anyway.bigbang.framework.beam.service.impl;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.beam.BeamContext;
import io.anyway.bigbang.framework.beam.BeamContextHolder;
import io.anyway.bigbang.framework.beam.domain.UserRouterStrategy;
import io.anyway.bigbang.framework.beam.domain.PredicateKey;
import io.anyway.bigbang.framework.beam.service.RouterStrategyCellular;
import io.anyway.bigbang.framework.beam.service.NodeRouterPredicate;
import io.anyway.bigbang.framework.beam.service.UserStrategyQuerier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Pattern;

@Slf4j
public class UserRouterPredicateCellularImpl implements NodeRouterPredicate, RouterStrategyCellular, UserStrategyQuerier {

    final public static String USER_PREFIX= "usr_";

    final public static String CLIENT_PREFIX= "cli_";

    private volatile boolean userStrategyUpdating = false;

    private ReadWriteLock readWriteLock= new ReentrantReadWriteLock();

    private Lock readLock= readWriteLock.readLock();

    private Lock writeLock= readWriteLock.writeLock();

    private Map<String, UserRouterStrategy> userRouterStrategyRepository= new HashMap<>();

    /**
     * {"trade#green": ["usr_t1_c_123","usr_t2_b_123","cli_t1_android_1.2.0","cli_t2_ios_1.2.1","usr_t1_c_.*[2|4|6|8|0]"]}
     */
    private Map<String, Set<Pattern>> userRouterStrategyMapping= new HashMap<>();

    @Override
    public boolean apply(PredicateKey predicateKey) {
        if(userStrategyUpdating){
            try {
                readLock.tryLock(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.error(e.getMessage(),e);
            } finally {
                readLock.unlock();
            }
        }
        String key= getUserRouterKey(predicateKey.getServiceId(),predicateKey.getUnit());
        if(userRouterStrategyMapping.containsKey(key)){
            Set<Pattern> patterns= userRouterStrategyMapping.get(key);
            BeamContext ctx= BeamContextHolder.getContext();
            List<String> candidates= new ArrayList<>(2);
            if(!StringUtils.isEmpty(ctx.getUid())) {
                String user = USER_PREFIX + ctx.getUid();
                candidates.add(user);
            }
            if(!StringUtils.isEmpty(ctx.getClientId())) {
                String cli = CLIENT_PREFIX + ctx.getClientId();
                candidates.add(cli);
            }
            return patterns.stream().anyMatch(p-> candidates.stream().anyMatch(i->p.matcher(i).matches()));
        }
        return true;
    }

    @Override
    public void setRouterStrategy(String id,String text){
        if(StringUtils.isEmpty(text)){
            return;
        }
        UserRouterStrategy strategy= JSONObject.parseObject(text,UserRouterStrategy.class);
        log.debug("Setting user router strategy: {}",strategy);
        try{
            writeLock.lock();
            userStrategyUpdating= true;
            removeUserRouterStrategy(id);
            userRouterStrategyRepository.put(id,strategy);
            Set<Pattern> patterns= new HashSet<>();
            if(strategy.getUsers()!= null){
                for(String usr: strategy.getUsers()){
                    patterns.add(Pattern.compile(usr));
                }
            }
            for(UserRouterStrategy.ServiceWrapper item: strategy.getServices()){
                String key= getUserRouterKey(item.getServiceId(),item.getUnit());
                Set<Pattern> userPatterns= userRouterStrategyMapping.get(key);
                if(userPatterns== null){
                    userRouterStrategyMapping.put(key,patterns);
                }
                else{
                    userPatterns.addAll(patterns);
                }
            }
            log.info("UserRouterStrategyMapping: {}",userRouterStrategyMapping);
        }
        finally {
            writeLock.unlock();
            userStrategyUpdating= false;
        }
    }

    @Override
    public void removeRouterStrategy(String id){
        log.debug("Removing user router strategy id: {}",id);
        try{
            writeLock.lock();
            userStrategyUpdating= true;
            removeUserRouterStrategy(id);
            log.info("UserRouterStrategyMapping: {}",userRouterStrategyMapping);
        }finally {
            writeLock.unlock();
            userStrategyUpdating= false;
        }
    }

    private void removeUserRouterStrategy(String id){
        if(userRouterStrategyRepository.containsKey(id)){
            UserRouterStrategy strategy= userRouterStrategyRepository.get(id);
            for(UserRouterStrategy.ServiceWrapper each: strategy.getServices()) {
                String key = getUserRouterKey(each.getServiceId(), each.getUnit());
                Set<Pattern> patterns= userRouterStrategyMapping.get(key);
                for(String user: strategy.getUsers()){
                    patterns.removeIf(pattern -> pattern.pattern().equals(user));
                }
                if(patterns.isEmpty()){
                    userRouterStrategyMapping.remove(key);
                }
                log.debug("remove user router strategy key: {}", key);
            }
            userRouterStrategyRepository.remove(id);
        }
    }

    private String getUserRouterKey(String serviceId,String unit){
        return (serviceId+"#"+unit).toLowerCase();
    }

    @Override
    public Collection<UserRouterStrategy> queryAll() {
        return userRouterStrategyRepository.values();
    }

    @Override
    public UserRouterStrategy query(String id) {
        return userRouterStrategyRepository.get(id);
    }
}
