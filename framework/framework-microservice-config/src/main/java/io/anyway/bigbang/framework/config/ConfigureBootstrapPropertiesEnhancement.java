package io.anyway.bigbang.framework.config;

import io.anyway.bigbang.framework.bootstrap.BootstrapPropertiesEnhancementEvent;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

public class ConfigureBootstrapPropertiesEnhancement implements BootstrapPropertiesEnhancementEvent {

    @Override
    public void process(ConfigurableEnvironment environment, Map<String, Object> additionalProperties) {
        additionalProperties.put("spring.cloud.nacos.config.enabled","false");
    }
}
