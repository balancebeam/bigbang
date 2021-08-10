package io.anyway.bigbang.framework.cache.config;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@ImportAutoConfiguration({RedissonAutoConfiguration.class})
public class CacheConfig {

}
