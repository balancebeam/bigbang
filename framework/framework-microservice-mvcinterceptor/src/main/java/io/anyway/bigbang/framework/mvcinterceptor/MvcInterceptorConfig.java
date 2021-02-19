package io.anyway.bigbang.framework.mvcinterceptor;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@Configuration
@ImportAutoConfiguration(ApiReturnValueConfig.class)
public class MvcInterceptorConfig {

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
