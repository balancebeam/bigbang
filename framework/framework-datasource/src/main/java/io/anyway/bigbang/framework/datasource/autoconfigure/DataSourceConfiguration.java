package io.anyway.bigbang.framework.datasource.autoconfigure;


import io.anyway.bigbang.framework.core.resource.SharedResourceExplorer;
import io.anyway.bigbang.framework.datasource.delegate.DefaultDelegatingDataSource;
import io.anyway.bigbang.framework.datasource.domain.DataSourceWrapper;
import io.anyway.bigbang.framework.datasource.property.DataSourceServiceProperties;
import io.anyway.bigbang.framework.datasource.service.DataSourceContextSelector;
import io.anyway.bigbang.framework.datasource.service.DataSourceTargetExchange;
import io.anyway.bigbang.framework.datasource.service.DataSourceDefinitionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;


@Slf4j
@EnableConfigurationProperties(DataSourceServiceProperties.class)
public class DataSourceConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataSourceContextSelector createDefaultDataSourceContextSelector(){
        return explorer -> explorer.getResourceByIndex(0);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceTargetExchange createDefaultDataSourceTargetExchange(){
        return ds -> ds;
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceDefinitionEvent createDefaultDataSourceDefinitionEvent(){
        return new DataSourceDefinitionEvent(){

            @Override
            public void onAdd(DataSourceWrapper target) {
            }

            @Override
            public void onRemove(DataSourceWrapper target) {
            }

            @Override
            public void onInit(SharedResourceExplorer<DataSourceWrapper> explorer) {
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultDelegatingDataSource createDefaultDelegatingDataSource(){
        DefaultDelegatingDataSource delegatingDataSource= new DefaultDelegatingDataSource();
        return delegatingDataSource;
    }
}
