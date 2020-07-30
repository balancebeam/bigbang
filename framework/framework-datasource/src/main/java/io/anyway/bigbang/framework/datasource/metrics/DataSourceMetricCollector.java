package io.anyway.bigbang.framework.datasource.metrics;

import com.alibaba.druid.pool.DruidDataSource;
import io.anyway.bigbang.framework.core.resource.SharedResourceExplorer;
import io.anyway.bigbang.framework.datasource.delegate.DefaultDelegatingDataSource;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

@Slf4j
public class DataSourceMetricCollector implements MeterBinder {

    @Autowired(required = false)
    private List<DataSource> dataSources= Collections.EMPTY_LIST;

    @Override
    public void bindTo(MeterRegistry registry) {
        for(DataSource each: dataSources){
            if(each instanceof DefaultDelegatingDataSource){
                ((SharedResourceExplorer<DruidDataSource>)each).forEachResource(
                  ds -> {
                      String name= ds.getName();
                      String prefix= "bigbang.datasource."+name+".";
                      Gauge.builder(prefix+"active_count", ds, x -> ds.getActiveCount()).register(registry);
                      Gauge.builder(prefix+"max_active_count", ds, x -> ds.getMaxActive()).register(registry);
                      Gauge.builder(prefix+"commit_count", ds, x -> ds.getCommitCount()).register(registry);
                      Gauge.builder(prefix+"connection_count", ds, x -> ds.getConnectCount()).register(registry);
                  }
                );
            }
        }
    }
}
