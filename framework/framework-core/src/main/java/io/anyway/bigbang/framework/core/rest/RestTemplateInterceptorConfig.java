package io.anyway.bigbang.framework.core.rest;

import io.anyway.bigbang.framework.core.interceptor.HeaderDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
public class RestTemplateInterceptorConfig {

    @Autowired(required = false)
    private List<RestTemplate> restTemplates = Collections.emptyList();

    @Resource
    private HeaderDeliveryService headerDeliveryService;

    @Bean
    public SmartInitializingSingleton extendsTemplateInitializer(
            final List<RestTemplateCustomizer> customizers) {
        return () -> {
            for (RestTemplate restTemplate : RestTemplateInterceptorConfig.this.restTemplates) {
                for (RestTemplateCustomizer customizer : customizers) {
                    customizer.customize(restTemplate);
                }
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public RestTemplateCustomizer xRestTemplateCustomizer() {
        log.info("init restTemplateCustomizer");
        return restTemplate -> {
            List<ClientHttpRequestInterceptor> list = new LinkedList<>(
                    restTemplate.getInterceptors());
            list.add((request, body, execution) -> {
                Map<String,String> headers= headerDeliveryService.headers();
                for(Map.Entry<String,String> each: headers.entrySet()){
                    request.getHeaders().add(each.getKey(),each.getValue());
                }
                return execution.execute(request, body);
            });
            restTemplate.setInterceptors(list);
        };
    }
}
