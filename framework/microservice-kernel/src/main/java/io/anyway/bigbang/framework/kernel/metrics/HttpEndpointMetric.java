package io.anyway.bigbang.framework.kernel.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
public class HttpEndpointMetric extends FilterRegistrationBean<Filter> implements MeterBinder {

    private Counter endpoint_qps;

    private Counter endpoint_tps;

    final private Set<String> QUERY_METHODS;

    final private Set<String> TRANSACTION_METHODS;

    public HttpEndpointMetric() {
        QUERY_METHODS = new HashSet<>(Arrays.asList("GET","TRACE"));
        TRANSACTION_METHODS = new HashSet<>(Arrays.asList("POST", "UPDATE", "DELETE"));

        this.setFilter(new GenericFilterBean() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                String method= ((HttpServletRequest)request).getMethod();
                try {
                    chain.doFilter(request, response);
                }finally {
                    count(method);
                }
            }
        });
        this.addUrlPatterns("/*");
        this.setName("HttpEndpointMeterBinder");
        this.setOrder(Ordered.HIGHEST_PRECEDENCE+1);
    }

    public void count(String method){
        method= method.toUpperCase();
        if(QUERY_METHODS.contains(method)){
            endpoint_qps.increment();
        }
        else if(TRANSACTION_METHODS.contains(method)){
            endpoint_tps.increment();
        }
        else{
            endpoint_qps.increment();
            log.debug("Not support statistic method: {}",method);
        }
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.endpoint_qps = Counter.builder("endpoint.qps_count").register(registry);
        this.endpoint_tps = Counter.builder("endpoint.tps_count").register(registry);
    }
}
