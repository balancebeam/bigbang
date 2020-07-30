package io.anyway.bigbang.framework.datasource.autoconfigure;

import io.anyway.bigbang.framework.datasource.metrics.DataSourceMetricCollector;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass({MeterBinder.class})
public class DataSourceMetricConfiguration {

    @Bean
    public DataSourceMetricCollector createDataSourceMetricCollector(){
        return new DataSourceMetricCollector();
    }
}
