package io.anyway.bigbang.framework.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.*;


@Slf4j
public class ApplicationDefaultPropertiesEventListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private Map<String,String> environments= new HashMap<>();

     {
         environments.put("k8s","k8s");
         environments.put("kubernetes","k8s");
         environments.put("istio","istio");
         environments.put("nacos","nacos");
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        MutablePropertySources source = event.getEnvironment().getPropertySources();
        source.addLast(new MapPropertySource("defaultProperties", collectProperties("default")));

        String active = event.getEnvironment().getProperty("spring.profiles.active");
        if (StringUtils.isEmpty(active)) {
            log.warn("You didn't assign any profile");
            return;
        }

        for (String name : environments.keySet()) {
            if (active.contains(name)) {
                name= environments.get(name);
                Map<String, Object> map = collectProperties(name);
                if (!CollectionUtils.isEmpty(map)) {
                    source.addBefore("defaultProperties", new MapPropertySource(name+"ProfileDefaultProperties", map));
                }
                return;
            }
        }
    }

    private Map<String, Object> collectProperties(String profile){
        Map<String, Object> defaultProperties = new HashMap<>();
        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:/META-INF/property/"+profile+".properties");
            Properties properties = new Properties();
            for (Resource each : resources) {
                try (InputStream in = each.getInputStream()) {
                    properties.load(in);
                    for (String name : properties.stringPropertyNames()) {
                        if(defaultProperties.containsKey(name)){
                            log.warn("the property {} will be overwrite,location: [}",name,each);
                        }
                        defaultProperties.put(name,properties.getProperty(name));
                    }
                }
                properties.clear();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return defaultProperties;
    }
}
