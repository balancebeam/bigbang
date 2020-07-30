package io.anyway.bigbang.framework.beam.service.impl;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.beam.BeamContext;
import io.anyway.bigbang.framework.beam.BeamContextHolder;
import io.anyway.bigbang.framework.beam.domain.UnitRouterStrategy;
import io.anyway.bigbang.framework.beam.domain.PredicateKey;
import io.anyway.bigbang.framework.beam.service.RouterStrategyCellular;
import io.anyway.bigbang.framework.beam.service.NodeRouterPredicate;
import io.anyway.bigbang.framework.beam.service.UnitStrategyQuerier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class UnitRouterPredicateCellularImpl implements NodeRouterPredicate, RouterStrategyCellular, UnitStrategyQuerier {



    private volatile boolean unitStrategyUpdating = false;

    private ReadWriteLock readWriteLock= new ReentrantReadWriteLock();

    private Lock readLock= readWriteLock.readLock();

    private Lock writeLock= readWriteLock.writeLock();

    private Map<String, UnitRouterStrategy> unitRouterStrategyRepository= new HashMap<>();

    /**
     * {"trade->order": { "blue": ["blue"], "green": ["green","red"]}}
     */
    private Map<String, Map<String, Set<String>>> unitRouterStrategyMapping= new HashMap<>();

    @Override
    public boolean apply(PredicateKey predicateKey) {
        if(unitStrategyUpdating){
            try {
                readLock.tryLock(1000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.error(e.getMessage(),e);
            } finally {
                readLock.unlock();
            }
        }
        BeamContext ctx= BeamContextHolder.getContext();
        String key= getUnitRouterKey(ctx.getSourceServiceId(),predicateKey.getServiceId());
        Map<String,Set<String>> unitMapping= unitRouterStrategyMapping.get(key);
        if(!CollectionUtils.isEmpty(unitMapping)){
            String targetServiceUnit= predicateKey.getUnit().toLowerCase();
            Set<String> units= unitMapping.get(ctx.getSourceServiceUnit());
            if(!CollectionUtils.isEmpty(units)){
                return units.contains(targetServiceUnit);
            }
        }
        String unit= BeamContextHolder.getContext().getUnit();
        if(!StringUtils.isEmpty(unit)){
            return unit.equals(predicateKey.getUnit());
        }
        return true;
    }

    @Override
    public void setRouterStrategy(String id,String text){
        if(StringUtils.isEmpty(text)){
            return;
        }
        UnitRouterStrategy strategy = JSONObject.parseObject(text,UnitRouterStrategy.class);
        log.debug("Setting unit router strategy: {}",strategy);
        try{
            writeLock.lock();
            unitStrategyUpdating= true;
            removeUnitRouterStrategy(id);
            unitRouterStrategyRepository.put(id,strategy);
            String key= getUnitRouterKey(strategy.getSourceServiceId(),strategy.getTargetServiceId());
            Map<String,Set<String>> unitMapping= unitRouterStrategyMapping.get(key);
            if(unitMapping==null){
                unitRouterStrategyMapping.put(key,unitMapping=new HashMap<>());
            }
            Set<String> units= unitMapping.get(strategy.getTargetServiceUnit().toLowerCase());
            if(units== null){
                unitMapping.put(strategy.getSourceServiceUnit().toLowerCase(),units= new HashSet<>());
            }
            units.add(strategy.getTargetServiceUnit().toLowerCase());
            log.info("UnitRouterStrategyMapping: {}",unitRouterStrategyMapping);
        }
        finally {
            writeLock.unlock();
            unitStrategyUpdating= false;
        }
    }

    @Override
    public void removeRouterStrategy(String id){
        log.debug("Removing unit router strategy id: {}",id);
        try{
            writeLock.lock();
            unitStrategyUpdating= true;
            removeUnitRouterStrategy(id);
            log.info("UnitRouterStrategyMapping: {}",unitRouterStrategyMapping);
        }finally {
            writeLock.unlock();
            unitStrategyUpdating= false;
        }
    }

    private void removeUnitRouterStrategy(String id){
        if(unitRouterStrategyRepository.containsKey(id)){
            UnitRouterStrategy strategy= unitRouterStrategyRepository.get(id);
            String key= getUnitRouterKey(strategy.getSourceServiceId(),strategy.getTargetServiceId());
            log.debug("remove unit router strategy key: {}",key);
            Map<String,Set<String>> unitMapping= unitRouterStrategyMapping.get(key);
            Set<String> units= unitMapping.get(strategy.getSourceServiceUnit());
            units.remove(strategy.getTargetServiceUnit());
            if(units.isEmpty()){
                unitMapping.remove(strategy.getSourceServiceUnit());
            }
            if(unitMapping.isEmpty()){
                unitRouterStrategyMapping.remove(key);
            }
            unitRouterStrategyRepository.remove(id);
        }
    }

    private String getUnitRouterKey(String sourceId,String targetId){
        return (sourceId+"->"+targetId).toLowerCase();
    }

    @Override
    public Collection<UnitRouterStrategy> queryAll() {
        return unitRouterStrategyRepository.values();
    }

    @Override
    public UnitRouterStrategy query(String id) {
        return unitRouterStrategyRepository.get(id);
    }

}
