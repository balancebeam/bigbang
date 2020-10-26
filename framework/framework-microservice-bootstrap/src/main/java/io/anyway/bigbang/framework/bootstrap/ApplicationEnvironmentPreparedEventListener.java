package io.anyway.bigbang.framework.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.HashMap;
import java.util.Map;


@Slf4j
public class ApplicationEnvironmentPreparedEventListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent applicationEnvironmentPreparedEvent) {
        MutablePropertySources source= applicationEnvironmentPreparedEvent.getEnvironment().getPropertySources();
        Map<String,Object> map= new HashMap<>();
        map.put("spring.cloud.kubernetes.enabled","false");
        map.put("spring.cloud.kubernetes.discovery.enabled","false");
        map.put("spring.cloud.kubernetes.loadbalancer.enabled","false");
        map.put("spring.cloud.nacos.discovery.enabled","false");
        map.put("logging.config","classpath:logging/logback-text-console.xml");
        map.put("spring.sleuth.enabled","false")
        //TODO find default properties at each component classpath*:META-INF/default.properties
        source.addLast(new MapPropertySource("module-default-properties",map));
    }

    private void f(){
        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        Properties properties = new Properties();
        try {
            Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:/META-INF/default.properties");
            for (Resource each : resources) {
                try (InputStream in = each.getInputStream()) {
                    properties.load(in);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
