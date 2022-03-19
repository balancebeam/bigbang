package io.anyway.bigbang.framework.exception.config;

import io.anyway.bigbang.framework.exception.RestTemplateResponseErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

@Slf4j
@Configuration
@ConditionalOnBean(RestTemplate.class)
public class RestTemplateErrorConfigure implements SmartInitializingSingleton {

    @Autowired(required = false)
    private List<RestTemplate> restTemplateList= Collections.emptyList();

    @Resource
    private RestTemplateResponseErrorHandler restTemplateResponseErrorHandler;

    @Override
    public void afterSingletonsInstantiated() {
        restTemplateList.stream().forEach(restTemplate -> {
            restTemplate.setErrorHandler(restTemplateResponseErrorHandler);
            log.info("RestTemplate: {} injected ErrorHandler: {}",restTemplate,restTemplateResponseErrorHandler);
        });
    }

}
