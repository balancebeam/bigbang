package io.anyway.bigbang.framework.beam.autoconfigure;

import com.netflix.client.config.IClientConfig;
import com.netflix.config.ConfigurationManager;
import com.netflix.loadbalancer.IRule;
import io.anyway.bigbang.framework.beam.loadbalancer.XZoneAvoidanceRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Slf4j
public class XRibbonClientsConfiguration {

	@Value(value = "${spring.bigbang.beam.zone-aware:false}")
    private boolean zoneAware;
	
	@Autowired(required = false)
    private IClientConfig config;
	
	@Bean
    public IRule ribbonRule() {
        if(false==zoneAware) {
            ConfigurationManager.getConfigInstance().setProperty("ZoneAwareNIWSDiscoveryLoadBalancer.enabled", "false");
            log.info("ZoneAwareNIWSDiscoveryLoadBalancer.enabled: {}",false);
        }
        XZoneAvoidanceRule rule = new XZoneAvoidanceRule();
        rule.initWithNiwsConfig(config);
        return rule;
    }
}
