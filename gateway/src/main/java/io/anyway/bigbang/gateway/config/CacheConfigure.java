package io.anyway.bigbang.gateway.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfigure {

    @Bean
    public CaffeineCacheManager createCaffeineCacheManager(){
        Caffeine caffeine = Caffeine.newBuilder()
                .initialCapacity(500)
                .maximumSize(5000)
                .expireAfterWrite(60, TimeUnit.SECONDS);
//                .expireAfter(new Expiry<Object, Object>() {
//                    @Override
//                    public long expireAfterCreate(Object key, Object value, long currentTime){
//                        return currentTime;
//                    }
//
//                    @Override
//                    public long expireAfterUpdate(Object key, Object value, long currentTime,  long currentDuration){
//                        return currentDuration;
//                    }
//
//                    @Override
//                    public long expireAfterRead(Object key, Object value, long currentTime,  long currentDuration){
//                        return currentDuration;
//                    }
//                });

        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setAllowNullValues(true);
        cacheManager.setCaffeine(caffeine);
        cacheManager.setCacheNames(Arrays.asList("MerchantPublicKey","MerchantApiAuthentication"));
        return cacheManager;
    }

}
