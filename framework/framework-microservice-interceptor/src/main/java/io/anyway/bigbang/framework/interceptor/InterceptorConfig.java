package io.anyway.bigbang.framework.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;


@Configuration
public class InterceptorConfig{

    @Bean
    public ApiHandlerInterceptor createApiHandlerInterceptor(RequestMappingHandlerAdapter requestMappingHandlerAdapter){
        return new ApiHandlerInterceptor(requestMappingHandlerAdapter.getMessageConverters());
    }

    @Bean
    public WebMvcInterceptorConfig createWebMvcInterceptorConfig(){
        return new WebMvcInterceptorConfig();
    }

}
