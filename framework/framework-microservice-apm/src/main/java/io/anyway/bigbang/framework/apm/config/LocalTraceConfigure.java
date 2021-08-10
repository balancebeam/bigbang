package io.anyway.bigbang.framework.apm.config;

import io.anyway.bigbang.framework.apm.DefaultLoggingTraceIdConverter;
import io.anyway.bigbang.framework.utils.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

@Slf4j
@Configuration
@ConditionalOnClass(ServletRequest.class)
@ConditionalOnProperty(name = "spring.sleuth.enabled",havingValue = "false")
public class LocalTraceConfigure {

    @Bean
    public GenericFilterBean createLocalTraceFilterBean() {
        return new GenericFilterBean(){
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                try{
                    String traceId= ((HttpServletRequest)request).getHeader("X-Trace-Id");
                    if(StringUtils.isEmpty(traceId)){
                        traceId = IdGenerator.next()+"";
                    }
                    DefaultLoggingTraceIdConverter.LOCAL_TRACE_HOLDER.set(traceId);
                    chain.doFilter(request,response);
                }finally {
                    DefaultLoggingTraceIdConverter.LOCAL_TRACE_HOLDER.remove();
                }
            }
        };
    }
}
