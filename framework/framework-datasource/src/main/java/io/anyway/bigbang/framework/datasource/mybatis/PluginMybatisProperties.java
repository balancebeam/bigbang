package io.anyway.bigbang.framework.datasource.mybatis;

import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PluginMybatisProperties extends MybatisProperties{

    public Resource[] resolveMapperLocations(ResourceLoader resourceLoader) {
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(resourceLoader);
        List<Resource> resources = new ArrayList<Resource>();
        if (this.getMapperLocations() != null) {
            for (String mapperLocation : this.getMapperLocations()) {
                try {
                    Resource[] mappers = resourceResolver.getResources(mapperLocation);
                    resources.addAll(Arrays.asList(mappers));
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }
}
