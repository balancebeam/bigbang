package io.anyway.bigbang.framework.feign;

import feign.RequestInterceptor;
import io.anyway.bigbang.framework.bootstrap.HeaderContextHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
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
