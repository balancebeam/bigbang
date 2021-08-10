package io.anyway.bigbang.framework.keystore;

import io.anyway.bigbang.framework.bootstrap.BootstrapPropertiesEnhancementEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Map;

@Slf4j
public class KeystoreBootstrapPropertiesEnhancement implements BootstrapPropertiesEnhancementEvent {

    @Override
    public void process(ConfigurableEnvironment environment, Map<String, Object> additionalProperties) {
        additionalProperties.put("spring.cloud.vault.enabled","false");
    }
}
