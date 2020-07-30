package io.anyway.bigbang.framework.datasource.sharding;

import com.alibaba.druid.pool.DruidDataSource;
import io.anyway.bigbang.framework.core.resource.SharedResourceExplorer;
import io.anyway.bigbang.framework.datasource.domain.DataSourceWrapper;
import io.anyway.bigbang.framework.datasource.service.DataSourceDefinitionEvent;
import io.anyway.bigbang.framework.datasource.service.DataSourceTargetExchange;
import io.anyway.bigbang.framework.datasource.sharding.delegate.ShardingDelegatingDataSource;
import io.anyway.bigbang.framework.datasource.sharding.exception.ShardingDataSourceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlShardingDataSourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DataSourceShardingRepository implements DataSourceDefinitionEvent, DataSourceTargetExchange {

    private DataSource defaultShardingDataSource;

    final private ConcurrentHashMap<String, DataSource> shardMapping= new ConcurrentHashMap<>();

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public DataSource mutate(DataSourceWrapper target) {
        if(DataSourceShardingContextHolder.sharding()){
            DataSource exchangeDataSource= shardMapping.get(target.getName());
            if(exchangeDataSource!= null){
                return exchangeDataSource;
            }
            DataSourceShardingContextHolder.setTargetDataSource(target);
            return defaultShardingDataSource;
        }
        return target;
    }

    @Override
    public void onAdd(DataSourceWrapper target) {
        String location = "classpath:/sharding-datasource/" + target.getName() + "/strategy.yml";
        Resource resource = resourceLoader.getResource(location);
        if(resource.exists()){
            DataSource ds= createShardingDataSource(resource,target);
            shardMapping.put(target.getName(),ds);
            log.info("Created ShardingDataSource: {}, location: {}",ds,location);
        }
    }

    @Override
    public void onRemove(DataSourceWrapper target) {
    }

    @Override
    public void onInit(SharedResourceExplorer<DataSourceWrapper> explorer) {
        String location = "classpath:/sharding-datasource/strategy.yml";
        Resource resource = resourceLoader.getResource(location);
        if(resource.exists()){
            defaultShardingDataSource= createShardingDataSource(resource,new ShardingDelegatingDataSource(explorer));
            log.info("Created DefaultShardingDataSource: {}, location: {}",defaultShardingDataSource,location);
        }
    }

    private DataSource createShardingDataSource(Resource resource,DataSource target) {
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("ds", target);
        try (InputStream in = resource.getInputStream()) {
            byte[] b = new byte[in.available()];
            in.read(b);
            return YamlShardingDataSourceFactory.createDataSource(dataSourceMap, b);
        } catch (Exception e) {
            log.error("Init ShardingDataSource error,strategy location: {}", resource, e);
            throw new ShardingDataSourceException();
        }

    }

}
