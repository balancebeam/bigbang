package io.anyway.bigbang.framework.datasource.sharding.autoconfigure;

import io.anyway.bigbang.framework.datasource.autoconfigure.DataSourceConfiguration;
import io.anyway.bigbang.framework.datasource.sharding.DataSourceShardingRepository;
import io.anyway.bigbang.framework.datasource.sharding.aspect.DataSourceShardingAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.context.annotation.Bean;

@Slf4j
@AutoConfigureBefore(DataSourceConfiguration.class)
public class DataSourceShardingConfiguration {

    @Bean
    public DataSourceShardingRepository createDataSourceShardingRepository(){
        return new DataSourceShardingRepository();
    }

    @Bean
    public DataSourceShardingAspect createDataSourceShardingAspect(){
        return new DataSourceShardingAspect();
    }
}
