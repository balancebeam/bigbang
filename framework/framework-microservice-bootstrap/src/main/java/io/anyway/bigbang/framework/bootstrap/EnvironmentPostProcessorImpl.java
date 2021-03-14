package io.anyway.bigbang.framework.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
public class EnvironmentPostProcessorImpl implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String version= environment.getProperty("spring.application.version");
        if(StringUtils.hasLength(version)){
            if(environment.getActiveProfiles().length>0) {
                List<String> profiles = new ArrayList<>(Arrays.asList(environment.getActiveProfiles()));
                for (int i = 0, j = profiles.size(); i < j; i++) {
                    profiles.add(profiles.get(i) + "-" + version);
                }
                environment.setActiveProfiles(StringUtils.toStringArray(profiles));
            }
            else{
                environment.setActiveProfiles(new String[]{version});
            }
        }
        MutablePropertySources mutablePropertySources= environment.getPropertySources();
        if(!mutablePropertySources.contains("springCloudDefaultProperties")){
            Map<String, Object> additionalProperties= new HashMap<>();
            ServiceLoader<BootstrapPropertiesEnhancementEvent> serviceLoader = ServiceLoader.load(BootstrapPropertiesEnhancementEvent.class);
            for(BootstrapPropertiesEnhancementEvent each: serviceLoader){
                each.process(environment,additionalProperties);
            }
            mutablePropertySources.addFirst(new MapPropertySource("overwriteBootstrapProperties",additionalProperties));
        }
        else{
            ServiceLoader<ApplicationPropertiesEnhancementEvent> serviceLoader = ServiceLoader.load(ApplicationPropertiesEnhancementEvent.class);
            Map<String, Object> additionalProperties= new HashMap<>();
            for(ApplicationPropertiesEnhancementEvent each: serviceLoader){
                each.process(environment,additionalProperties);
            }
            mutablePropertySources.addLast(new MapPropertySource("defaultApplicationProperties",additionalProperties));
        }
    }
}
