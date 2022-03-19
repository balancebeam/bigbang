package io.anyway.bigbang.framework.interceptor.mvc;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;

@Configuration
@ImportAutoConfiguration(ApiReturnValueConfigure.class)
@ConditionalOnClass(HttpServletRequest.class)
public class MvcInterceptorConfigure {

    @Bean
    public ApiHandlerInterceptor createApiHandlerInterceptor(){
        return new ApiHandlerInterceptor();
    }

    @Bean
    public WebMvcInterceptorConfig createWebMvcInterceptorConfig(){
        return new WebMvcInterceptorConfig();
    }
}
