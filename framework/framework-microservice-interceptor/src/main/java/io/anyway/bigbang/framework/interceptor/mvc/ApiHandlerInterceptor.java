package io.anyway.bigbang.framework.interceptor.mvc;

import io.anyway.bigbang.framework.security.mask.ApiMaskSensitive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class ApiHandlerInterceptor  implements WebHandlerInterceptor {

    final static String STOPWATCH= "framework.stopWatch";

    final static String APIMASKSENSITIVE= "apiMaskSensitive";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        StopWatch stopWatch= new StopWatch();
        stopWatch.start();
        request.setAttribute(STOPWATCH,stopWatch);
        if(handler instanceof HandlerMethod){
            ApiMaskSensitive apiMaskSensitive= ((HandlerMethod)handler).getMethod().getAnnotation(ApiMaskSensitive.class);
            if(apiMaskSensitive!= null){
                request.setAttribute(APIMASKSENSITIVE,"TRUE");
            }
        }
        return true;
    }
}
