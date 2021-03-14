package io.anyway.bigbang.framework.bootstrap;

import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

public interface ApplicationPropertiesEnhancementEvent {

    void process(ConfigurableEnvironment environment, Map<String,Object> additionalProperties);
}
