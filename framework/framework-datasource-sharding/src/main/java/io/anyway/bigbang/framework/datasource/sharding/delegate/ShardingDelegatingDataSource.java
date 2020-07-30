package io.anyway.bigbang.framework.datasource.sharding.delegate;

import com.alibaba.druid.pool.DruidDataSource;
import io.anyway.bigbang.framework.core.resource.SharedResourceExplorer;
import io.anyway.bigbang.framework.datasource.domain.DataSourceWrapper;
import io.anyway.bigbang.framework.datasource.sharding.DataSourceShardingContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;

@Slf4j
public class ShardingDelegatingDataSource extends DelegatingDataSource{

    private SharedResourceExplorer<DataSourceWrapper> explorer;

    public ShardingDelegatingDataSource(SharedResourceExplorer<DataSourceWrapper> explorer){
        this.explorer= explorer;
    }

    @Override
    final public DataSource getTargetDataSource() {
        DataSource ds= DataSourceShardingContextHolder.getTargetDataSource();
        if(ds== null){
            ds= explorer.getResourceByIndex(0);
        }
        return ds;
    }

    @Override
    public void afterPropertiesSet() {
        //do nothing
    }


}
