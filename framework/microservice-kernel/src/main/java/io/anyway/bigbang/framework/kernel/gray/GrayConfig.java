package io.anyway.bigbang.framework.kernel.gray;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IRule;
import io.anyway.bigbang.framework.kernel.header.PrincipleHeaderKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Configuration
@ImportAutoConfiguration({NacosMetadataMapConfig.class,GrayConfig.RibbonRuleConfig.class})
@RibbonClients(defaultConfiguration = GrayConfig.RibbonRuleConfig.class)
public class GrayConfig {

    @Bean
    public PrincipleHeaderKey createGreyUnitHeaderKey(){
        return () -> NacosMetadataMapConfig.GRAY_UNIT_NAME;
    }

    @Bean
    public PrincipleHeaderKey createGreyIndicatorHeaderKey(){
        return () -> NacosMetadataMapConfig.GRAY_INDICATOR_NAME;
    }

    public static class RibbonRuleConfig{
        @Autowired(required = false)
        private IClientConfig config;

        @Bean
        public IRule ribbonRule() {
            GrayRibbonRule rule = new GrayRibbonRule();
            rule.initWithNiwsConfig(config);
            return rule;
        }
    }
}
