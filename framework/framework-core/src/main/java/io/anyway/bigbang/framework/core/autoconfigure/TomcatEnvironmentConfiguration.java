package io.anyway.bigbang.framework.core.autoconfigure;

import io.anyway.bigbang.framework.core.client.ClientAgentGenericFilterBean;
import io.anyway.bigbang.framework.core.security.InternalApiInterceptor;
import io.anyway.bigbang.framework.core.security.SecurityContextGenericFilterBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.startup.Tomcat;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@Slf4j
@ConditionalOnClass(Tomcat.class)
public class TomcatEnvironmentConfiguration {

    @Bean
    public FilterRegistrationBean clientAgentFilter() {
        log.debug("clientAgentFilter FilterRegistrationBean start");
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new ClientAgentGenericFilterBean());
        registration.addUrlPatterns("/*");
        registration.setName("clientAgentFilter");
        registration.setOrder(0);
        return registration;
    }

    @Bean
    public FilterRegistrationBean userDetailFilter() {
        log.debug("userDetailFilter FilterRegistrationBean start");
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new SecurityContextGenericFilterBean());
        registration.addUrlPatterns("/*");
        registration.setName("userDetailFilter");
        registration.setOrder(1);
        return registration;
    }

    @Bean
    public InternalApiInterceptor createInternalApiInterceptor(){
        return new InternalApiInterceptor();
    }

}
