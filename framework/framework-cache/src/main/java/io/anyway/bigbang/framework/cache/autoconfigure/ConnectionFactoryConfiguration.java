package io.anyway.bigbang.framework.cache.autoconfigure;

import io.anyway.bigbang.framework.cache.service.RedisConnectionFactoryBuilder;
import io.anyway.bigbang.framework.cache.service.impl.JedisConnectionFactoryBuilder;
import io.anyway.bigbang.framework.cache.service.impl.LettuceConnectionFactoryBuilder;
import io.anyway.bigbang.framework.cache.service.impl.RedissonConnectionFactoryBuilder;
import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.Jedis;

public class ConnectionFactoryConfiguration {

    @Slf4j
    @AutoConfigureBefore({BasicCacheConfiguration.class,LettuceCacheConfiguration.class})
    @ConditionalOnClass(Jedis.class)
    public static class JedisCacheConfiguration{
        @Bean
        public RedisConnectionFactoryBuilder createJedisConnectionFactoryBuilder(){
            return new JedisConnectionFactoryBuilder();
        }
    }

    @Slf4j
    @AutoConfigureBefore({BasicCacheConfiguration.class,LettuceCacheConfiguration.class})
    @ConditionalOnClass(Redisson.class)
    public static class RedissonCacheConfiguration{
        @Bean
        public RedisConnectionFactoryBuilder createRedissonConnectionFactoryBuilder(){
            return new RedissonConnectionFactoryBuilder();
        }
    }

    @Slf4j
    @AutoConfigureBefore(BasicCacheConfiguration.class)
    @ConditionalOnClass(RedisClient.class)
    public static class LettuceCacheConfiguration{
        @Bean
        @ConditionalOnMissingBean
        public RedisConnectionFactoryBuilder createLettuceConnectionFactoryBuilder(){
            return new LettuceConnectionFactoryBuilder();
        }
    }
}
