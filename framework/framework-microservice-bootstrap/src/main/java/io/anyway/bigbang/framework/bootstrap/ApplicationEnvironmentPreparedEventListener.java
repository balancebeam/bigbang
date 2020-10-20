package io.anyway.bigbang.framework.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

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
        map.put("spring.log.home","/var/log/app/");
        //TODO find default properties at each component classpath*:META-INF/default.properties
        source.addLast(new MapPropertySource("module-default-properties",map));
    }
}
