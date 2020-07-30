package io.anyway.bigbang.framework.cache.autoconfigure;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import io.anyway.bigbang.framework.cache.CacheService;
import io.anyway.bigbang.framework.cache.distributedlock.LockService;
import io.anyway.bigbang.framework.cache.distributedlock.impl.DefaultLockServiceImpl;
import io.anyway.bigbang.framework.cache.property.RedisConfigProperties;
import io.anyway.bigbang.framework.cache.service.RedisConnectionFactorySelector;
import io.anyway.bigbang.framework.cache.service.CacheKeyWrapperProcessor;
import io.anyway.bigbang.framework.cache.delegate.DelegatingRedisConnectionFactory;
import io.anyway.bigbang.framework.cache.service.impl.RedisCacheServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@EnableConfigurationProperties(RedisConfigProperties.class)
public class BasicCacheConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheKeyWrapperProcessor createDefaultCacheKeyWrapperService() {
        return key-> key;
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisConnectionFactorySelector createDefaultRedisConnectionFactorySelector() {
        return (explorer)-> explorer.getResourceByIndex(0);
    }

    @Bean("redisCacheService")
    public CacheService createRedisCacheService(){
        return new RedisCacheServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public DelegatingRedisConnectionFactory createDelegatingRedisConnectionFactory(){
        DelegatingRedisConnectionFactory delegate= new DelegatingRedisConnectionFactory();
        log.info("Init DelegatingRedisConnectionFactory: {}",delegate);
        return delegate;
    }

    @Bean
    public RedisTemplate<String,Object> redisTemplate(DelegatingRedisConnectionFactory redisConnectionFactory) {
        RedisTemplate template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        template.setDefaultSerializer(new FastJsonRedisSerializer<>(Object.class));
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public LockService createDefaultLockService(){
        return new DefaultLockServiceImpl();
    }

}
