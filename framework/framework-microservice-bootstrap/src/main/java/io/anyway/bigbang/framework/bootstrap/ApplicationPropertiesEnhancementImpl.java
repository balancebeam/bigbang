package io.anyway.bigbang.framework.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.*;

@Slf4j
public class ApplicationPropertiesEnhancementImpl implements ApplicationPropertiesEnhancementEvent{

    private Set<String> platforms= new HashSet<>();

    {
        platforms.add("k8s");
        platforms.add("istio");
        platforms.add("nacos");
    }

    @Override
    public void process(ConfigurableEnvironment environment, Map<String, Object> additionalProperties) {
        String version= environment.getProperty("spring.application.version");
        additionalProperties.putAll(collectProperties("default"));
        //added version to nacos register center when the application was running the nacos profile environment
        if(!StringUtils.isEmpty(version)) {
            additionalProperties.put("spring.cloud.nacos.discovery.metadata.version", version);
        }
        String platform = environment.getProperty("spring.cloud.discovery.platform");
        log.info("spring.cloud.discovery.platform: {}",platform);
        if (platforms.contains(platform)) {
            Map<String, Object> map = collectProperties(platform);
            additionalProperties.putAll(map);
        }
    }

    private Map<String, Object> collectProperties(String platform){
        Map<String, Object> defaultProperties = new HashMap<>();
        PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:/META-INF/property/"+platform+".properties");
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
