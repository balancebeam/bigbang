package io.anyway.bigbang.framework.gray;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IRule;
import io.anyway.bigbang.framework.bootstrap.HeaderContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.GenericFilterBean;


@Configuration
@ImportAutoConfiguration({GrayConfig.RibbonConfig.class})
public class GrayConfig {

    @Bean
    public HeaderContext createGrayRouteHeaderContext(){
        return new HeaderContext() {
            @Override
            public String getName() {
                return GrayContext.GRAY_NAME;
            }

            @Override
            public void removeThreadLocal() {
                GrayContextHolder.removeGrayContext();
            }
        };
    }

    @Slf4j
    @Configuration
    @ConditionalOnDiscoveryEnabled
    @ConditionalOnClass(GenericFilterBean.class)
    @RibbonClients(defaultConfiguration = RibbonConfig.class)
    public static class RibbonConfig{

        @Autowired(required = false)
        private IClientConfig config;

        @Bean
        @ConditionalOnNacosDiscoveryEnabled
        public IRule nacosRibbonRule() {
            NacosGrayRibbonRule rule = new NacosGrayRibbonRule();
            rule.initWithNiwsConfig(config);
            return rule;
        }

        @Bean
        @ConditionalOnKubernetesDiscoveryEnabled
        public IRule kubernetesRibbonRule() {
            KubernetesGrayRibbonRule rule = new KubernetesGrayRibbonRule();
            rule.initWithNiwsConfig(config);
            return rule;
        }

        @Bean
        @ConditionalOnIstioDiscoveryEnabled
        public IRule istioRibbonRule() {
            IstioGrayRibbonRule rule = new IstioGrayRibbonRule();
            rule.initWithNiwsConfig(config);
            return rule;
        }
    }

}
