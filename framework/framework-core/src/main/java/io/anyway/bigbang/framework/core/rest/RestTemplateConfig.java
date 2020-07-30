package io.anyway.bigbang.framework.core.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


@Slf4j
@ImportAutoConfiguration(RestTemplateInterceptorConfig.class)
public class RestTemplateConfig {

    @Value("${spring.bigbang.rest-template.connection-timeout:15000}")
    private int restTemplateConnectionTimeout;

    @Value("${spring.bigbang.rest-template.read-timeout:15000}")
    private int restTemplateReadTimeout;

    @Bean("simpleClientHttpRequestFactory")
    public ClientHttpRequestFactory simpleClientHttpRequestFactory(){
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(restTemplateConnectionTimeout);
        factory.setReadTimeout(restTemplateReadTimeout);
        return factory;
    }

    @Bean
    public RestTemplate restTemplate(@Qualifier("simpleClientHttpRequestFactory") ClientHttpRequestFactory factory){
        RestTemplate restTemplate= new RestTemplate(factory);
        return restTemplate;
    }

}
