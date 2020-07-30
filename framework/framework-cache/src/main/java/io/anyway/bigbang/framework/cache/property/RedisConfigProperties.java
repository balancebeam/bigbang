package io.anyway.bigbang.framework.cache.property;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "spring.bigbang.redis")
public class RedisConfigProperties extends XRedisProperties{

    private Map<String,XRedisProperties> tenantRedisConfig= Collections.emptyMap();
    private Map<String,String> tenantRedisMapping= Collections.emptyMap();
}
