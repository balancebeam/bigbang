package io.anyway.bigbang.framework.datasource.delegate;

import io.anyway.bigbang.framework.core.resource.SharedResourceExplorer;
import io.anyway.bigbang.framework.core.resource.SharedResourceVisitor;
import io.anyway.bigbang.framework.datasource.domain.DataSourceWrapper;
import io.anyway.bigbang.framework.datasource.property.DataSourceServiceProperties;
import io.anyway.bigbang.framework.datasource.service.DataSourceContextSelector;
import io.anyway.bigbang.framework.datasource.service.DataSourceTargetExchange;
import io.anyway.bigbang.framework.datasource.service.DataSourceDefinitionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.util.StringUtils;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DefaultDelegatingDataSource extends DelegatingDataSource implements SharedResourceExplorer<DataSourceWrapper>, ApplicationListener<ContextRefreshedEvent> {

    final private ConcurrentHashMap<String, DataSourceWrapper> mapping = new ConcurrentHashMap<>();

    final private CopyOnWriteArrayList<DataSourceWrapper> list = new CopyOnWriteArrayList<>();

    @Resource
    private DataSourceServiceProperties dataSourceServiceProperties;

    @Resource
    private DataSourceContextSelector dataSourceSelector;

    @Resource
    private DataSourceTargetExchange targetExchange;

    @Resource
    private DataSourceDefinitionEvent dataSourceEvent;

    final public void add(DataSourceWrapper ds){
        list.add(ds);
        mapping.put(ds.getName(),ds);
        dataSourceEvent.onAdd(ds);
    }

    final public void remove(String name){
        DataSourceWrapper ds= mapping.remove(name);
        if(ds!= null) {
            list.remove(ds);
            dataSourceEvent.onRemove(ds);
        }
    }

    @Override
    final public DataSource getTargetDataSource() {
        DataSourceWrapper ds=  dataSourceSelector.choose(this);
        return targetExchange.mutate(ds);
    }

    @Override
    public void afterPropertiesSet() {
        if(!dataSourceServiceProperties.getTenantDatasourceConfig().isEmpty()) {
            dataSourceServiceProperties.getTenantDatasourceConfig().entrySet().forEach(each->{
                each.getValue().setName(each.getKey());
                add(each.getValue());
            });
        }
        if(mapping.isEmpty()){
            throw new IllegalArgumentException("Target DataSource was empty.");
        }
        log.info("DataSources: {}",mapping);
        dataSourceEvent.onInit(this);
    }

    @PreDestroy
    public void destroy(){
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if(event.getApplicationContext().getParent() == null){

        }
    }

    @Override
    public DataSourceWrapper getResourceByName(String name) {
        return mapping.get(name);
    }

    @Override
    public int getResourceSize() {
        return list.size();
    }

    @Override
    public DataSourceWrapper getResourceByIndex(int index) {
        return list.get(index);
    }

    @Override
    public void forEachResource(SharedResourceVisitor<DataSourceWrapper> visitor) {
        list.forEach(resource->visitor.visit(resource));
    }
}
