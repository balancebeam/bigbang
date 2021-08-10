package io.anyway.bigbang.framework.interceptor.mvc;

import io.aanyway.bigbang.framework.model.api.ApiResponseEntity;
import io.anyway.bigbang.framework.security.mask.MaskSensitiveDataContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.StopWatch;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

@Slf4j
public class ApiReturnValueProcessor extends RequestResponseBodyMethodProcessor {

    public ApiReturnValueProcessor(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        Method method = returnType.getMethod();
        if (method != null && method.getReturnType().isAssignableFrom(ApiResponseEntity.class)) {
            return true;
        }
        return super.supportsParameter(returnType);
    }

    @Override
    public void handleReturnValue(Object returnValue,
                                  MethodParameter returnType,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest) throws IOException, HttpMediaTypeNotAcceptableException {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes)requestAttributes).getRequest();
        if(returnValue instanceof ApiResponseEntity){
            StopWatch stopWatch= (StopWatch)request.getAttribute(ApiHandlerInterceptor.STOPWATCH);
            if(stopWatch!= null){
                stopWatch.stop();
                ((ApiResponseEntity)returnValue).setDuration(stopWatch.getTotalTimeMillis());
            }
        }
        try {
            if ("TRUE".equals(request.getAttribute(ApiHandlerInterceptor.APIMASKSENSITIVE))) {
                MaskSensitiveDataContextHolder.anchorExecutorMarkSensitive();
            }
            super.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
        }finally {
            MaskSensitiveDataContextHolder.resetExecutorMarkSensitive();
        }
    }
}
