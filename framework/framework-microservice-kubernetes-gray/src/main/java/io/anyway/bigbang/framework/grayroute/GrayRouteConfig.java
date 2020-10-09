package io.anyway.bigbang.framework.grayroute;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IRule;
import io.anyway.bigbang.framework.discovery.NacosGrayRouteConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@ImportAutoConfiguration({NacosGrayRouteConfig.class, GrayRouteConfig.RibbonRuleConfig.class})
@RibbonClients(defaultConfiguration = GrayRouteConfig.RibbonRuleConfig.class)
public class GrayRouteConfig {

    public static class RibbonRuleConfig{
        @Autowired(required = false)
        private IClientConfig config;

        @Bean
        public IRule ribbonRule() {
            GrayRouteRibbonRule rule = new GrayRouteRibbonRule();
            rule.initWithNiwsConfig(config);
            return rule;
        }
    }
}
