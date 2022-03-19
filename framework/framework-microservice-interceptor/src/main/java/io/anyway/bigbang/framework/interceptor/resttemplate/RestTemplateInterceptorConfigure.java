package io.anyway.bigbang.framework.interceptor.resttemplate;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
public class RestTemplateInterceptorConfigure implements SmartInitializingSingleton {

    @Autowired(required = false)
    private List<RestTemplate> restTemplateList= Collections.emptyList();

    @Autowired(required = false)
    private List<WebClientHttpRequestInterceptor> interceptorList= Collections.emptyList();

    @Override
    public void afterSingletonsInstantiated() {
        restTemplateList.stream().forEach(restTemplate -> {
            List<ClientHttpRequestInterceptor> list= restTemplate.getInterceptors();
            if(CollectionUtils.isEmpty(list)){
                restTemplate.setInterceptors(list= new ArrayList<>());
            }
            list.addAll(interceptorList);
            log.info("RestTemplate: {} injected WebClientHttpRequestInterceptor list: {}",restTemplate,list);
        });
    }

}
