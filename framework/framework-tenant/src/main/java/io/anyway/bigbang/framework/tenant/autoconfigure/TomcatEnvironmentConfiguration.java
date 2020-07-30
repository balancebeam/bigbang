package io.anyway.bigbang.framework.tenant.autoconfigure;

import io.anyway.bigbang.framework.tenant.TenantContextHolder;
import io.anyway.bigbang.framework.tenant.TenantDetail;
import io.anyway.bigbang.framework.tenant.mock.MockService;
import io.anyway.bigbang.framework.tenant.mock.impl.MockServiceImpl;
import io.anyway.bigbang.framework.tenant.proxy.PluginManagement;
import io.anyway.bigbang.framework.tenant.proxy.PluginDispatcherServlet;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.startup.Tomcat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.HandlerExecutionChain;

import javax.annotation.Resource;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@ConditionalOnClass(Tomcat.class)
public class TomcatEnvironmentConfiguration {

    @Value("${spring.bigbang.tenant.mock.tenant-name:}")
    private String mockTenant;

    @Resource
    private PluginManagement pluginManagement;

    @Bean
    public MockService createMockService(){
        return new MockServiceImpl();
    }

    @Bean
    public FilterRegistrationBean tenantFilter(final MockService mockService) {
        log.debug("tenantFilter FilterRegistrationBean start");
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new GenericFilterBean(){
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                HttpServletRequest httpRequest = (HttpServletRequest)request;
                String tenantId= httpRequest.getHeader(TenantDetail.HEADER_TENANT_KEY);
                try {
                    if(!StringUtils.isEmpty(tenantId)){
                        if(mockTenant.equals(tenantId)) {
                            mockService.invoke((HttpServletRequest) request, (HttpServletResponse) response);
                            return;
                        }
                        TenantContextHolder.setTenantDetail(new TenantDetail(tenantId));
                        AnnotationConfigApplicationContext ctx= pluginManagement.getApplicationContext(tenantId);
                        if(ctx!= null){
                            PluginDispatcherServlet dispatcherServlet= ctx.getBean(PluginDispatcherServlet.class);
                            try {
                                HandlerExecutionChain handler= dispatcherServlet.getAvailableHandler(httpRequest);
                                if(handler!= null) {
                                    PluginDispatcherServlet.setHandler(handler);
                                    dispatcherServlet.service(request, response);
                                    return;
                                }
                            } catch (Exception e) {
                                log.error("execute tenant {} servlet error",tenantId,e);
                            }
                            finally {
                                PluginDispatcherServlet.removeHandler();
                            }
                        }
                    }
                    chain.doFilter(request, response);
                }finally {
                    TenantContextHolder.remove();
                }
            }
        });
        registration.addUrlPatterns("/*");
        registration.setName("tenantFilter");
        registration.setOrder(Ordered.LOWEST_PRECEDENCE);
        return registration;
    }


}
