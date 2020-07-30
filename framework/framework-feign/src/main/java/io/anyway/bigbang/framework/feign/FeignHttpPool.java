package io.anyway.bigbang.framework.feign;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

@Configuration
public class FeignHttpPool {

    @Bean
    public HttpClient httpClient() {

        Builder requestConfigBuilder = RequestConfig.custom();
        requestConfigBuilder.setSocketTimeout(20000);
        requestConfigBuilder.setConnectTimeout(20000);

        PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager(30, TimeUnit.SECONDS);
        pollingConnectionManager.setMaxTotal(5000);
        pollingConnectionManager.setDefaultMaxPerRoute(100);

        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
        httpClientBuilder.setConnectionManager(pollingConnectionManager);
        httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
        HttpClient client = httpClientBuilder.build();

        Timer timer = new Timer();
        
        timer.schedule(new TimerTask() {
        	
            @Override
            public void run() {
                pollingConnectionManager.closeExpiredConnections();
                pollingConnectionManager.closeIdleConnections(5, TimeUnit.SECONDS);
            }
        }, 10000, 5000);

        return client;
    }
}
