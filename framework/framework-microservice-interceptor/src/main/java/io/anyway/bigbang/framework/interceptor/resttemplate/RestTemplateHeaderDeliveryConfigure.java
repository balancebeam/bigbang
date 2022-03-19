package io.anyway.bigbang.framework.interceptor.resttemplate;

import io.anyway.bigbang.framework.header.HeaderContextHolder;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;

@Configuration
@ConditionalOnBean(RestTemplate.class)
@ImportAutoConfiguration(RestTemplateInterceptorConfigure.class)
public class RestTemplateHeaderDeliveryConfigure {

    @Bean
    public WebClientHttpRequestInterceptor createHeaderDeliveryClientHttpRequestInterceptor(){
        return (request, body, execution) -> {
            Collection<String> headerNames= HeaderContextHolder.getHeaderNames();
            for(String name: headerNames){
                request.getHeaders().add(name,HeaderContextHolder.getHeaderValue(name).get());
            }
            return execution.execute(request,body);
        };
    }
}
