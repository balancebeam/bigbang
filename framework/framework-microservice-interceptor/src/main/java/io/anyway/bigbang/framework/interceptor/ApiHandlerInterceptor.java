package io.anyway.bigbang.framework.kernel.interceptor;

import io.anyway.bigbang.framework.kernel.api.APIResponse;
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
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

@Slf4j
public class ApiHandlerInterceptor extends RequestResponseBodyMethodProcessor implements HandlerInterceptor {

    final private String STOPWATCH= "framework.stopWatch";

    public ApiHandlerInterceptor(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        StopWatch stopWatch= new StopWatch();
        stopWatch.start();
        request.setAttribute(STOPWATCH,stopWatch);
        return true;
    }

    @Override
    public boolean supportsReturnType(MethodParameter returnType) {
        Method method = returnType.getMethod();
        if (method != null && method.getReturnType().isAssignableFrom(ApiResponse.class)) {
            return true;
        }
        return super.supportsParameter(returnType);
    }

    @Override
    public void handleReturnValue(Object returnValue, MethodParameter returnType, ModelAndViewContainer mavContainer, NativeWebRequest webRequest) throws IOException, HttpMediaTypeNotAcceptableException {
        if(returnValue instanceof APIResponse){
            RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
            StopWatch stopWatch= (StopWatch)((ServletRequestAttributes)requestAttributes).getRequest().getAttribute(STOPWATCH);
            if(stopWatch!= null){
                stopWatch.stop();
                //((APIResponse)returnValue).setDuration(stopWatch.getTotalTimeMillis());
            }
        }
        super.handleReturnValue(returnValue, returnType, mavContainer, webRequest);
    }
}
