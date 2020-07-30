package io.anyway.bigbang.framework.cache.delegate;

import io.anyway.bigbang.framework.cache.property.RedisConfigProperties;
import io.anyway.bigbang.framework.cache.property.XRedisProperties;
import io.anyway.bigbang.framework.cache.service.RedisConnectionFactoryBuilder;
import io.anyway.bigbang.framework.cache.service.RedisConnectionFactorySelector;
import io.anyway.bigbang.framework.core.resource.SharedResourceExplorer;
import io.anyway.bigbang.framework.core.resource.SharedResourceVisitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DelegatingRedisConnectionFactory implements RedisConnectionFactory, SharedResourceExplorer<RedisConnectionFactory> {

    @Resource
    private RedisConnectionFactorySelector redisConnectionFactorySelector;

    @Resource
    private RedisConnectionFactoryBuilder redisConnectionFactoryBuilder;

    @Resource
    private RedisConfigProperties redisServiceProperties;

    final private CopyOnWriteArrayList<RedisConnectionFactory> redisConnectionFactoryList = new CopyOnWriteArrayList<>();

    final private ConcurrentHashMap<String,RedisConnectionFactory> redisConnectionFactoryMapping = new ConcurrentHashMap<>();

    protected RedisConnectionFactory getTarget(){
        return redisConnectionFactorySelector.select(this);
    }

    public void add(XRedisProperties redisProperties){
        RedisConnectionFactory redisConnectionFactory= redisConnectionFactoryBuilder.build(redisProperties);
        redisConnectionFactoryList.add(redisConnectionFactory);
        redisConnectionFactoryMapping.put(redisProperties.getName(),redisConnectionFactory);
    }

    public void add(List<XRedisProperties> redisPropertiesList){
        redisPropertiesList.forEach(p-> add(p));

    }

    public void remove(String name){
        RedisConnectionFactory target= redisConnectionFactoryMapping.remove(name);
        if(target!= null) {
            redisConnectionFactoryBuilder.destroy(target);
            redisConnectionFactoryList.remove(target);
        }
    }

    @Override
    public RedisConnection getConnection() {
        return getTarget().getConnection();
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        return getTarget().getClusterConnection();
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return getTarget().getConvertPipelineAndTxResults();
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        return getTarget().getSentinelConnection();
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        return getTarget().translateExceptionIfPossible(ex);
    }

    @PostConstruct
    public void init(){
        if(!redisServiceProperties.getTenantRedisConfig().isEmpty()){
            redisServiceProperties.getTenantRedisConfig().entrySet().forEach(each->{
                each.getValue().setName(each.getKey());
                add(each.getValue());
            });
        }
        else{
            redisServiceProperties.setName("redis");
            add(redisServiceProperties);
        }
       if(redisConnectionFactoryList.isEmpty()){
            throw new IllegalArgumentException("Redis config was empty,you must configure spring.bigang.redis");
        }
    }

    @PreDestroy
    public void destroy(){
        redisConnectionFactoryList.forEach(f-> redisConnectionFactoryBuilder.destroy(f));
    }

    @Override
    public RedisConnectionFactory getResourceByName(String name) {
        return redisConnectionFactoryMapping.get(name);
    }

    @Override
    public int getResourceSize() {
        return redisConnectionFactoryList.size();
    }

    @Override
    public RedisConnectionFactory getResourceByIndex(int index) {
        return redisConnectionFactoryList.get(index);
    }

    @Override
    public void forEachResource(SharedResourceVisitor<RedisConnectionFactory> visitor) {
        redisConnectionFactoryList.forEach(resource->visitor.visit(resource));
    }
}
