package io.anyway.bigbang.framework.interceptor.mvc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Configuration
@ConditionalOnClass(HttpServletRequest.class)
public class ApiReturnValueConfigure {

    @Resource
    private List<HandlerMethodReturnValueHandler> handlerMethodReturnValueHandlers= Collections.emptyList();

    @Resource
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @PostConstruct
    public void init(){
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
