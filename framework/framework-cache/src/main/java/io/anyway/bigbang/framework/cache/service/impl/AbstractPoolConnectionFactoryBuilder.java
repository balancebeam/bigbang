package io.anyway.bigbang.framework.cache.service.impl;

import io.anyway.bigbang.framework.cache.property.XRedisProperties;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractPoolConnectionFactoryBuilder {

    protected RedisSentinelConfiguration createRedisSentinelConfiguration (XRedisProperties redisProperties){
        String master= redisProperties.getSentinel().getMaster();
        List<String> nodes= redisProperties.getSentinel().getNodes();
        Set<String> sentinelHostAndPorts = new HashSet<>(nodes);
        RedisSentinelConfiguration configuration= new RedisSentinelConfiguration(master,sentinelHostAndPorts);
        if (!StringUtils.isEmpty(redisProperties.getPassword())) {
            RedisPassword redisPassword = RedisPassword.of(redisProperties.getPassword());
            configuration.setPassword(redisPassword);
        }
        return configuration;
    }

    protected RedisClusterConfiguration createRedisClusterConfiguration (XRedisProperties redisProperties){
        List<String> nodes=  redisProperties.getCluster().getNodes();
        RedisClusterConfiguration configuration= new RedisClusterConfiguration(nodes);
        configuration.setMaxRedirects(redisProperties.getCluster().getMaxRedirects());
        if (!StringUtils.isEmpty(redisProperties.getPassword())) {
            RedisPassword redisPassword = RedisPassword.of(redisProperties.getPassword());
            configuration.setPassword(redisPassword);
        }
        return configuration;
    }

    protected RedisStandaloneConfiguration createRedisStandaloneConfiguration (XRedisProperties redisProperties){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisProperties.getHost());
        configuration.setPort(redisProperties.getPort());
        configuration.setDatabase(redisProperties.getDatabase());
        if (!StringUtils.isEmpty(redisProperties.getPassword())) {
            RedisPassword redisPassword = RedisPassword.of(redisProperties.getPassword());
            configuration.setPassword(redisPassword);
        }
        return configuration;
    }


}
