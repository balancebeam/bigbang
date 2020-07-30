package io.anyway.bigbang.framework.metrics;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.startup.Tomcat;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
public class HttpEndpointFilterMetricCollector extends FilterRegistrationBean  {

    @Resource
    private HttpEndpointMetricCollector httpEndpointMetricCollector;

    public HttpEndpointFilterMetricCollector(){

        this.setFilter(new GenericFilterBean() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
               String method= ((HttpServletRequest)request).getMethod();
                httpEndpointMetricCollector.count(method);
               chain.doFilter(request,response);
            }
        });
        this.addUrlPatterns("/*");
        this.setName("httpEndpointMetricFilter");
        this.setOrder(Ordered.HIGHEST_PRECEDENCE+1);
    }


}
