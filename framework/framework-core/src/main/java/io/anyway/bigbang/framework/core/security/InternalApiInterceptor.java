package io.anyway.bigbang.framework.core.security;

import io.anyway.bigbang.framework.core.exception.BigbangException;
import io.anyway.bigbang.framework.core.security.annotation.InternalApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Slf4j
public class InternalApiInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String bool= request.getHeader(InternalApi.HEADER_GATEWAY_KEY);
        if("true".equals(bool)) {
            if (handler instanceof HandlerMethod) {
                Method method = ((HandlerMethod) handler).getMethod();
                if (method != null) {
                    if(method.isAnnotationPresent(InternalApi.class)){
                        throw new BigbangException(BigbangException.UNAUTHORIZED_ERROR_CODE) {};
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception { }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {}
}
