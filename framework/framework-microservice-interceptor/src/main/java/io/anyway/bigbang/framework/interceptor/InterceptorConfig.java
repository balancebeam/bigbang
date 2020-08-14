package io.anyway.bigbang.framework.interceptor;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@Configuration
@ImportAutoConfiguration(ApiReturnValueConfig.class)
public class InterceptorConfig {

    @Bean
    public ApiHandlerInterceptor createApiHandlerInterceptor(){
        return new ApiHandlerInterceptor();
    }

    @Bean
    public ApiReturnValueProcessor createApiReturnValueProcessor(RequestMappingHandlerAdapter requestMappingHandlerAdapter){
        return new ApiReturnValueProcessor(requestMappingHandlerAdapter.getMessageConverters());
    }

    @Bean
    public WebMvcInterceptorConfig createWebMvcInterceptorConfig(){
        return new WebMvcInterceptorConfig();
    }
}
