package io.anyway.bigbang.framework.kernel.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class WebMvcInterceptorConfig implements WebMvcConfigurer {

    @Autowired(required = false)
    private List<HandlerInterceptor> interceptors= Collections.emptyList();

    @Autowired(required = false)
    private List<HandlerMethodReturnValueHandler> handlerMethodReturnValueHandlers= Collections.emptyList();

    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if(!CollectionUtils.isEmpty(interceptors)) {
            for(HandlerInterceptor each: interceptors) {
                registry.addInterceptor(each).addPathPatterns("/**").excludePathPatterns("/mq/**");
            }
        }
        if(!handlerMethodReturnValueHandlers.isEmpty()) {
            final List<HandlerMethodReturnValueHandler> originalHandlers = requestMappingHandlerAdapter.getReturnValueHandlers();
            if (!CollectionUtils.isEmpty(originalHandlers)) {
                final List<HandlerMethodReturnValueHandler> newHandlers = new LinkedList<>();
                newHandlers.addAll(originalHandlers);
                // 获取处理器应处于的位置，需要在RequestResponseBodyMethodProcessor之前
                final int index = obtainValueHandlerPosition(originalHandlers, RequestResponseBodyMethodProcessor.class);
                newHandlers.addAll(index, handlerMethodReturnValueHandlers);
                requestMappingHandlerAdapter.setReturnValueHandlers(newHandlers);
            } else {
                requestMappingHandlerAdapter.setReturnValueHandlers(handlerMethodReturnValueHandlers);
            }

        }
    }

    private int obtainValueHandlerPosition(final List<HandlerMethodReturnValueHandler> originalHandlers, Class<?> handlerClass) {
        for (int i = 0; i < originalHandlers.size(); i++) {
            final HandlerMethodReturnValueHandler valueHandler = originalHandlers.get(i);
            if (handlerClass.isAssignableFrom(valueHandler.getClass())) {
                return i;
            }
        }
        return -1;
    }
}
