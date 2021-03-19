package io.anyway.bigbang.framework.interceptor.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;

public class WebMvcInterceptorConfig implements WebMvcConfigurer {

    @Autowired(required = false)
    private List<WebHandlerInterceptor> interceptors= Collections.emptyList();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if(!CollectionUtils.isEmpty(interceptors)) {
            for(HandlerInterceptor each: interceptors) {
                registry.addInterceptor(each).addPathPatterns("/**").excludePathPatterns("/mq/**");
            }
        }
    }
}
