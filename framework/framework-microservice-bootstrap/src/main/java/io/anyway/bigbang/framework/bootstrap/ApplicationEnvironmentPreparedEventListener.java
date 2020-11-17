package io.anyway.bigbang.framework.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.*;


@Slf4j
public class ApplicationEnvironmentPreparedEventListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private List<String[]> environments= new ArrayList<>();

     {
        environments.add(new String[]{"kubernetes","k8s"});
        environments.add(new String[]{"nacos"});
        environments.add(new String[]{"istio"});
    }

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        Map<String, Map<String, Object>> defaultEnvPropertiesMap = new HashMap<>();
        Map<String, Object> defaultProperties = new HashMap<>();
        MutablePropertySources source = event.getEnvironment().getPropertySources();
        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:/META-INF/default.properties");
            Properties properties = new Properties();
            for (Resource each : resources) {
                try (InputStream in = each.getInputStream()) {
                    properties.load(in);
                    for (String name : properties.stringPropertyNames()) {
                        if (name.contains("/")) {
                            String[] np = name.split("/");
                            Map<String, Object> m = defaultEnvPropertiesMap.get(np[0]);
                            if (m == null) {
                                defaultEnvPropertiesMap.put(np[0], m = new HashMap<>());
                            }
                            m.put(np[1], properties.get(name));
                        } else {
                            defaultProperties.put(name, properties.get(name));
                        }
                    }
                }
                properties.clear();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        source.addLast(new MapPropertySource("defaultModuleProperties", defaultProperties));

        String active = event.getEnvironment().getProperty("spring.profiles.active");
        if (StringUtils.isEmpty(active)) {
            log.warn("You didn't assign any profile");
            return;
        }
        for (String[] names : environments) {
            for (String each : names) {
                if (active.contains(each)) {
                    Map<String, Object> map = new HashMap<>();
                    for (String n : names) {
                        Map<String, Object> m = defaultEnvPropertiesMap.get(n);
                        if (m != null) {
                            map.putAll(m);
                        }
                    }
                    if (!CollectionUtils.isEmpty(map)) {
                        source.addBefore("defaultModuleProperties", new MapPropertySource(names[0] + "ModuleProperties", map));
                    }
                    return;
                }
            }
        }
    }

}
