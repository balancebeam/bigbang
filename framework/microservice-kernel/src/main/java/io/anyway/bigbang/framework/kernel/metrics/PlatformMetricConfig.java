package io.anyway.bigbang.framework.kernel.metrics;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlatformMetricConfig {

    @Bean
    public ExecutorMetric createExecutorMetric(){
        return new ExecutorMetric();
    }

    @Bean
    public TomcatMetric createTomcatMetric(){
        return new TomcatMetric();
    }

    @Bean
    public HttpEndpointMetric createHttpEndpointMetric(){
        return new HttpEndpointMetric();
    }

}
