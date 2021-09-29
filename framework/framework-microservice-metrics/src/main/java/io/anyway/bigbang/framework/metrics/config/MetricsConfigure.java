package io.anyway.bigbang.framework.metrics.config;

import io.anyway.bigbang.framework.metrics.TomcatMetricCollector;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(Connector.class)
public class MetricsConfigure {

    @Bean
    public TomcatMetricCollector createTomcatMetric(){
        return new TomcatMetricCollector();
    }

}
