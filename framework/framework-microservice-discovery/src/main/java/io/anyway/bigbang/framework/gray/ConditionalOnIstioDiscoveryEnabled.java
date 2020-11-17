package io.anyway.bigbang.framework.gray;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ConditionalOnProperty(value = "spring.cloud.istio.discovery.enabled",matchIfMissing = true)
public @interface ConditionalOnIstioDiscoveryEnabled {

}