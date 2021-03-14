package io.anyway.bigbang.framework.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class ApplicationPropertiesEnhancementImpl implements ApplicationPropertiesEnhancementEvent{

    private Map<String,String> environments= new HashMap<>();

    {
        environments.put("k8s","k8s");
        environments.put("kubernetes","k8s");
        environments.put("istio","istio");
        environments.put("nacos","nacos");
    }

    @Override
    public void process(ConfigurableEnvironment environment, Map<String, Object> additionalProperties) {
        String version= environment.getProperty("spring.application.version");
        if(StringUtils.isEmpty(version)){
            log.error("spring.application.version must be not empty");
            throw new RuntimeException("spring.application.version must be not empty");
        }
        additionalProperties.putAll(collectProperties("default"));
        //added version to nacos register center when the application was running the nacos profile environment
        additionalProperties.put("spring.cloud.nacos.discovery.metadata.version",version);
        String active = environment.getProperty("spring.profiles.active");
        if (!StringUtils.isEmpty(active)) {
            for (String name : environments.keySet()) {
                if (active.contains(name)) {
                    name= environments.get(name);
                    Map<String, Object> map = collectProperties(name);
                    additionalProperties.putAll(map);
                }
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
