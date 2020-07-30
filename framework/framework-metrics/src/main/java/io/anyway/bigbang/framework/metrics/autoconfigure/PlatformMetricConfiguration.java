package io.anyway.bigbang.framework.metrics.autoconfigure;

import io.anyway.bigbang.framework.metrics.ExecutorThreadMetricCollector;
import io.anyway.bigbang.framework.metrics.HttpEndpointMetricCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;

@Slf4j
public class PlatformMetricConfiguration {

    @Bean
    public HttpEndpointMetricCollector createHttpEndpointMetricCollector(){
        return new HttpEndpointMetricCollector();
    }

    @Bean
    public ExecutorThreadMetricCollector createExecutorThreadMetricCollector(){
        return new ExecutorThreadMetricCollector();
    }
}
