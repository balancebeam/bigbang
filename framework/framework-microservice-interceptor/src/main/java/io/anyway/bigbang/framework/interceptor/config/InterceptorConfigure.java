package io.anyway.bigbang.framework.interceptor.config;

import io.anyway.bigbang.framework.interceptor.feign.FeignConfigure;
import io.anyway.bigbang.framework.interceptor.mvc.MvcInterceptorConfigure;
import io.anyway.bigbang.framework.interceptor.resttemplate.RestTemplateHeaderDeliveryConfigure;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@ImportAutoConfiguration({
        MvcInterceptorConfigure.class,
        RestTemplateHeaderDeliveryConfigure.class,
        FeignConfigure.class})
public class InterceptorConfigure  {

}
