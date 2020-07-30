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
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.util.StringUtils;

@Slf4j
public class JedisConnectionFactoryBuilder extends AbstractPoolConnectionFactoryBuilder implements RedisConnectionFactoryBuilder<JedisConnectionFactory> {

    @Override
    public JedisConnectionFactory build(XRedisProperties redisProperties) {
        JedisClientConfiguration.DefaultJedisClientConfigurationBuilder builder =
                (JedisClientConfiguration.DefaultJedisClientConfigurationBuilder) JedisClientConfiguration.builder();
        if(redisProperties.getTimeout()!= null) {
            builder.connectTimeout(redisProperties.getTimeout());
            builder.readTimeout(redisProperties.getTimeout());
        }
        builder.usePooling();
        if(redisProperties.getClientName()!= null){
            builder.clientName(redisProperties.getClientName());
        }
        GenericObjectPoolConfig genericObjectPoolConfig= createGenericObjectPoolConfig(redisProperties);
        builder.poolConfig(genericObjectPoolConfig);
        JedisConnectionFactory jedisConnectionFactory= null;
        if(redisProperties.getSentinel() != null) {
            RedisSentinelConfiguration configuration= createRedisSentinelConfiguration(redisProperties);
            jedisConnectionFactory= new JedisConnectionFactory(configuration, builder.build());
        }
        else if(redisProperties.getCluster()!= null){
            RedisClusterConfiguration configuration= createRedisClusterConfiguration(redisProperties);
            jedisConnectionFactory= new JedisConnectionFactory(configuration, builder.build());
        }
        else if(!StringUtils.isEmpty(redisProperties.getHost())){
            RedisStandaloneConfiguration configuration = createRedisStandaloneConfiguration(redisProperties);
            jedisConnectionFactory= new JedisConnectionFactory(configuration, builder.build());
        }
        if(jedisConnectionFactory!= null){
            jedisConnectionFactory.afterPropertiesSet();
            return jedisConnectionFactory;
        }
        throw new InvalidConfigException(JSONObject.toJSONString(redisProperties));
    }

    @Override
    public void destroy(JedisConnectionFactory f) {
        try {
            f.destroy();
        } catch (Exception e) {
            log.error("Destroy JedisConnectionFactory error :{}",f,e);
        }
    }

    private GenericObjectPoolConfig createGenericObjectPoolConfig(XRedisProperties redisProperties){
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        RedisProperties.Pool pool= redisProperties.getJedis().getPool();
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
