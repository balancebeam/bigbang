package io.anyway.bigbang.framework.metrics;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FrameworkMetricsConfig {

    @Bean
    public TomcatMetricCollector createTomcatMetric(){
        return new TomcatMetricCollector();
    }

    @Bean
    public HttpEndpointMetricCollector createHttpEndpointMetric(){
        return new HttpEndpointMetricCollector();
    }

}
