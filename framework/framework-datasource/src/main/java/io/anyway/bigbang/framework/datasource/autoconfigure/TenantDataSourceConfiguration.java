package io.anyway.bigbang.framework.datasource.autoconfigure;

import io.anyway.bigbang.framework.datasource.domain.DataSourceWrapper;
import io.anyway.bigbang.framework.datasource.exception.NoFoundDataSourceException;
import io.anyway.bigbang.framework.datasource.mybatis.PluginMybatisComponentClassBuilder;
import io.anyway.bigbang.framework.datasource.service.DataSourceContextSelector;
import io.anyway.bigbang.framework.tenant.TenantContextHolder;
import io.anyway.bigbang.framework.tenant.TenantDetail;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;


@ConditionalOnClass(TenantDetail.class)
@AutoConfigureBefore(DataSourceConfiguration.class)
public class TenantDataSourceConfiguration {

    @Bean
    public DataSourceContextSelector createTenantDataSourceContextSelector(){
        return explorer -> {
            TenantDetail tenantDetail= TenantContextHolder.getTenantDetail();
            if(tenantDetail== null){
                return explorer.getResourceByIndex(0);
            }
            DataSourceWrapper ds= explorer.getResourceByName(tenantDetail.getTenantId());
            if(ds== null){
                throw new NoFoundDataSourceException(tenantDetail.getTenantId());
            }
            return ds;
        };
    }

    @Bean
    public PluginMybatisComponentClassBuilder createMybatisPluginComponentClassBuilder(){
        return new PluginMybatisComponentClassBuilder();
    }
}
