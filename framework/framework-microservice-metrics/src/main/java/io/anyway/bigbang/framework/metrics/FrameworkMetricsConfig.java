package io.anyway.bigbang.framework.metrics;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(TomcatConnectorCustomizer.class)
public class FrameworkMetricsConfig {

    @Bean
    public TomcatMetricCollector createTomcatMetric(){
        return new TomcatMetricCollector();
    }

}
