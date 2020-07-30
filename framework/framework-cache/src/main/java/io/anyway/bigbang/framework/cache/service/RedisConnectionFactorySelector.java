package io.anyway.bigbang.framework.cache.service;

import io.anyway.bigbang.framework.core.resource.SharedResourceExplorer;
import org.springframework.data.redis.connection.RedisConnectionFactory;

public interface RedisConnectionFactorySelector {

    RedisConnectionFactory select(SharedResourceExplorer<RedisConnectionFactory> explorer);
}
