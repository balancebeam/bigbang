package io.anyway.bigbang.framework.interceptor.feign;

import feign.Feign;
import feign.RequestInterceptor;
import io.anyway.bigbang.framework.bootstrap.HeaderContextHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
@ConditionalOnClass(Feign.class)
public class FeignConfig {

    @Bean
    public RequestInterceptor createDeliveryHeaderFeignInterceptor(){
        return template -> {
            Collection<String> headerNames= HeaderContextHolder.getHeaderNames();
            for(String name: headerNames){
                template.header(name,HeaderContextHolder.getHeaderValue(name).get());
            }
        };
    }

    @Bean
    public BeanDefinitionRegistryPostProcessor createNoRegisterFeignClientProcessor(){
        return new NoRegisterFeignClientProcessor();
    }
}
