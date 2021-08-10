package io.anyway.bigbang.framework.interceptor.mvc;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Configuration
@EnableWebMvc
@ImportAutoConfiguration(ApiReturnValueConfigure.class)
@ConditionalOnClass(HttpServletRequest.class)
public class MvcInterceptorConfigure {

    @Resource
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Bean
    public ApiHandlerInterceptor createApiHandlerInterceptor(){
        return new ApiHandlerInterceptor();
    }

    @Bean
    public ApiReturnValueProcessor createApiReturnValueProcessor(){
        return new ApiReturnValueProcessor(requestMappingHandlerAdapter.getMessageConverters());
    }

    @Bean
    public WebMvcInterceptorConfig createWebMvcInterceptorConfig(){
        return new WebMvcInterceptorConfig();
    }
}
