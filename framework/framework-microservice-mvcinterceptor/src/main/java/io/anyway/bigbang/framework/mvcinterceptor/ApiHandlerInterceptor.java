package io.anyway.bigbang.framework.mvcinterceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class ApiHandlerInterceptor  implements WebHandlerInterceptor {

    final static String STOPWATCH= "framework.stopWatch";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        StopWatch stopWatch= new StopWatch();
        stopWatch.start();
        request.setAttribute(STOPWATCH,stopWatch);
        return true;
    }


}
