package io.anyway.bigbang.framework.metrics.autoconfigure;

import io.anyway.bigbang.framework.metrics.HttpEndpointFilterMetricCollector;
import io.anyway.bigbang.framework.metrics.TomcatMetricCollector;
import org.apache.catalina.startup.Tomcat;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass(Tomcat.class)
@AutoConfigureAfter(PlatformMetricConfiguration.class)
public class TomcatEnvironmentConfiguration {

    @Bean
    public TomcatMetricCollector createTomcatMeterBinder(){
        return new TomcatMetricCollector();
    }

    @Bean
    public HttpEndpointFilterMetricCollector createHttpEndpointFilterMetricCollector(){
        return new HttpEndpointFilterMetricCollector();
    }
}
