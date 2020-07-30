package io.anyway.bigbang.framework.cache.service.impl;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.cache.exception.InvalidConfigException;
import io.anyway.bigbang.framework.cache.service.RedisConnectionFactoryBuilder;
import io.anyway.bigbang.framework.cache.property.XRedisProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.util.StringUtils;

@Slf4j
public class LettuceConnectionFactoryBuilder extends AbstractPoolConnectionFactoryBuilder implements RedisConnectionFactoryBuilder<LettuceConnectionFactory> {

    @Override
    public LettuceConnectionFactory build(XRedisProperties redisProperties) {
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder= LettucePoolingClientConfiguration.builder();
        if(redisProperties.getTimeout()!= null) {
            builder.commandTimeout(redisProperties.getTimeout());
        }
        if(redisProperties.getLettuce()!= null &&
                redisProperties.getLettuce().getShutdownTimeout()!= null){
            builder.shutdownTimeout(redisProperties.getLettuce().getShutdownTimeout());
        }
        if(redisProperties.getClientName()!= null){
            builder.clientName(redisProperties.getClientName());
        }
        builder.poolConfig(createGenericObjectPoolConfig(redisProperties));
        LettuceConnectionFactory lettuceConnectionFactory= null;
        if(redisProperties.getSentinel() != null) {
            RedisSentinelConfiguration configuration= createRedisSentinelConfiguration(redisProperties);
            lettuceConnectionFactory= new LettuceConnectionFactory(configuration,builder.build());
        }
        else if(redisProperties.getCluster()!= null){
            RedisClusterConfiguration configuration= createRedisClusterConfiguration(redisProperties);
            lettuceConnectionFactory= new LettuceConnectionFactory(configuration,builder.build());
        }
        else if(!StringUtils.isEmpty(redisProperties.getHost())){
            RedisStandaloneConfiguration configuration = createRedisStandaloneConfiguration(redisProperties);
            lettuceConnectionFactory= new LettuceConnectionFactory(configuration,builder.build());
        }
        if(lettuceConnectionFactory!= null){
            lettuceConnectionFactory.afterPropertiesSet();
            return lettuceConnectionFactory;
        }
        throw new InvalidConfigException(JSONObject.toJSONString(redisProperties));
    }

    @Override
    public void destroy(LettuceConnectionFactory f) {
        try {
            f.destroy();
        } catch (Exception e) {
            log.error("Destroy LettuceConnectionFactory error :{}",f,e);
        }
    }

    private GenericObjectPoolConfig createGenericObjectPoolConfig(XRedisProperties redisProperties){
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        RedisProperties.Pool pool= redisProperties.getLettuce().getPool();
        if(pool== null){
            pool = new RedisProperties.Pool();
        }
        genericObjectPoolConfig.setMaxTotal(pool.getMaxActive());
        genericObjectPoolConfig.setMinIdle(pool.getMinIdle());
        genericObjectPoolConfig.setMaxIdle(pool.getMaxIdle());
        genericObjectPoolConfig.setMaxWaitMillis(pool.getMaxWait().toMillis());
        if(pool.getTimeBetweenEvictionRuns()!= null) {
            genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(pool.getTimeBetweenEvictionRuns().toMillis());
        }
        return genericObjectPoolConfig;
    }
}
