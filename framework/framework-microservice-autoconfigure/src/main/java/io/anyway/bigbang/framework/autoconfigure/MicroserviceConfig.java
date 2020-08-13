package io.anyway.bigbang.framework.autoconfigure;

import io.anyway.bigbang.framework.datasource.DataSourceConfig;
import io.anyway.bigbang.framework.exception.FeignClientErrorDecoder;
import io.anyway.bigbang.framework.exception.GlobalExceptionHandler;
import io.anyway.bigbang.framework.executor.TransmittableTaskExecutionConfig;
import io.anyway.bigbang.framework.grayroute.GrayRouteConfig;
import io.anyway.bigbang.framework.header.HeaderContext;
import io.anyway.bigbang.framework.header.HeaderContextHolder;
import io.anyway.bigbang.framework.interceptor.InterceptorConfig;
import io.anyway.bigbang.framework.logging.marker.LoggingMarkerAspect;
import io.anyway.bigbang.framework.metrics.FrameworkMetricsConfig;
import io.anyway.bigbang.framework.swagger.Swagger2Config;
import io.anyway.bigbang.framework.useragent.UserAgentContextConfig;
import io.anyway.bigbang.framework.utils.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.security.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@AutoConfigureBefore(TaskExecutionAutoConfiguration.class)
@ImportAutoConfiguration({
        InterceptorConfig.class,
        LoggingMarkerAspect.class,
        FeignClientErrorDecoder.class,
        TransmittableTaskExecutionConfig.class,
        FrameworkMetricsConfig.class,
        SecurityConfig.class,
        DataSourceConfig.class,
        UserAgentContextConfig.class,
        GlobalExceptionHandler.class,
        GrayRouteConfig.class,
        Swagger2Config.class,
        IdGenerator.class
})
public class MicroserviceConfig {

    @Autowired(required = false)
    private List<HeaderContext> HeaderContextList= Collections.emptyList();

    @Bean
    public GenericFilterBean createHeaderCollectionFilterBean() {
        return new GenericFilterBean(){
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                try{
                    Map<String,String> headers= new HashMap<>();
                    HeaderContextList.stream().forEach(each-> {
                        String value= ((HttpServletRequest)request).getHeader(each.getName());
                        if(!StringUtils.isEmpty(value)) {
                            headers.put(each.getName(), value);
                        }
                    });
                    HeaderContextHolder.setHeaderMapping(headers);
                    log.debug("headers: {}",headers);
                    chain.doFilter(request,response);
                }finally {
                    HeaderContextHolder.remove();
                    HeaderContextList.stream().forEach(each-> each.removeThreadLocal());
                }
            }
        };
    }
}
