package io.anyway.bigbang.framework.cache.service;

import io.anyway.bigbang.framework.cache.property.XRedisProperties;
import org.springframework.data.redis.connection.RedisConnectionFactory;

public interface RedisConnectionFactoryBuilder<T extends RedisConnectionFactory>{

    T build(XRedisProperties redisProperties);

    void destroy(T f);

}
