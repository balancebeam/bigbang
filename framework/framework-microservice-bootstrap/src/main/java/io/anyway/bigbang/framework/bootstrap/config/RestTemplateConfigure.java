package io.anyway.bigbang.framework.bootstrap.config;

import io.anyway.bigbang.framework.bootstrap.ssl.SSLContextGenerator;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@Configuration
@ConditionalOnMissingBean(RestTemplate.class)
public class RestTemplateConfigure {

    @Value("${spring.restTemplate.keystore.location:}")
    private String location;

    @Value("${spring.restTemplate.keystore.password:}")
    private String password;

    @Value("${spring.restTemplate.readTimeout:5000}")
    private int readTimeout;

    @Value("${spring.restTemplate.connectTimeout:2000}")
    private int connectTimeout;

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(createClientHttpRequestFactory());
        return restTemplate;
    }

    private ClientHttpRequestFactory createClientHttpRequestFactory(){
        if(StringUtils.isEmpty(location)){
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setReadTimeout(readTimeout);
            requestFactory.setConnectTimeout(connectTimeout);
            return requestFactory;
        }
        SSLContext sslContext= SSLContextGenerator.generate(location,password);
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setReadTimeout(readTimeout);
        requestFactory.setConnectTimeout(connectTimeout);
        return requestFactory;
    }
}
