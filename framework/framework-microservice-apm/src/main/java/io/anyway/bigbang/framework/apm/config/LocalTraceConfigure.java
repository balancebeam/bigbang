package io.anyway.bigbang.framework.apm.config;

import io.anyway.bigbang.framework.header.HeaderContext;
import io.anyway.bigbang.framework.header.HeaderContextHolder;
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

    final public static String TRACE_HEADER_NAME = "X-Trace-Id";

    @Bean
    public GenericFilterBean createLocalTraceFilterBean() {
        return new GenericFilterBean(){
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                String traceId= ((HttpServletRequest)request).getHeader(TRACE_HEADER_NAME);
                if(StringUtils.isEmpty(traceId)){
                    traceId = IdGenerator.nextRadixId("TRC");
                    HeaderContextHolder.addHeader(TRACE_HEADER_NAME,traceId);
                }
                chain.doFilter(request,response);
            }
        };
    }

    @Bean
    public HeaderContext createApmHeaderContext() {

        return new HeaderContext() {

            @Override
            public String getName() {
                return TRACE_HEADER_NAME;
            }

            public void removeThreadLocal() {
                HeaderContextHolder.removeHeader(TRACE_HEADER_NAME);
            }
        };
    }
}
