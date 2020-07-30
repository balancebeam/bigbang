package io.anyway.bigbang.framework.cache.autoconfigure;

import io.anyway.bigbang.framework.cache.property.RedisConfigProperties;
import io.anyway.bigbang.framework.cache.service.RedisConnectionFactorySelector;
import io.anyway.bigbang.framework.cache.service.CacheKeyWrapperProcessor;
import io.anyway.bigbang.framework.core.utils.XStringUtils;
import io.anyway.bigbang.framework.tenant.TenantContextHolder;
import io.anyway.bigbang.framework.tenant.TenantDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.annotation.Resource;

@Slf4j
@AutoConfigureBefore(BasicCacheConfiguration.class)
@ConditionalOnClass(TenantDetail.class)
public class TenantCacheConfiguration {

    @Resource
    private RedisConfigProperties redisConfigProperties;

    @Bean
    public CacheKeyWrapperProcessor createTenantCacheKeyWrapperService() {
        return key -> {
            TenantDetail tenantDetail= TenantContextHolder.getTenantDetail();
            if(tenantDetail!= null){
                return "tenant_"+tenantDetail.getTenantId()+"#"+key;
            }
            return key;
        };
    }

    @Bean
    public RedisConnectionFactorySelector createTenantRedisConnectionFactorySelector() {
        return (explorer)-> {
            TenantDetail tenantDetail= TenantContextHolder.getTenantDetail();
            if(tenantDetail!= null) {
                String target=redisConfigProperties.getTenantRedisMapping().get(tenantDetail.getTenantId());
                if(target== null){
                    target= tenantDetail.getTenantId();
                }
                RedisConnectionFactory redisConnectionFactory = explorer.getResourceByName(target);
                if (redisConnectionFactory != null) {
                    return redisConnectionFactory;
                }
                int index= XStringUtils.toHash(tenantDetail.getTenantId()) % explorer.getResourceSize();
                return explorer.getResourceByIndex(index);
            }
            return explorer.getResourceByIndex(0);
        };
    }
}
