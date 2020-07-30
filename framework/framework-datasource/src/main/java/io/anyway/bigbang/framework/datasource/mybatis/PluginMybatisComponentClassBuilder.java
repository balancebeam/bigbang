package io.anyway.bigbang.framework.datasource.mybatis;

import io.anyway.bigbang.framework.tenant.proxy.PluginComponentClassBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.PropertySource;

import java.util.Iterator;

public class PluginMybatisComponentClassBuilder implements PluginComponentClassBuilder {

    @Override
    public Class<?> build(AnnotationConfigApplicationContext ctx) {
        Iterator<PropertySource<?>> iterator=  ctx.getEnvironment().getPropertySources().iterator();
        for(;iterator.hasNext();){
            if(iterator.next().containsProperty("mybatis.mapper-locations")){
                return PluginMybatisAutoConfiguration.class;
            }
        }
        return null;
    }
}
