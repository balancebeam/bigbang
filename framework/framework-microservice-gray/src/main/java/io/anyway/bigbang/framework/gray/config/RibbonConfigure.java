package io.anyway.bigbang.framework.gray.config;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import io.anyway.bigbang.framework.discovery.ConditionalOnIstioDiscoveryEnabled;
import io.anyway.bigbang.framework.discovery.ConditionalOnKubernetesDiscoveryEnabled;
import io.anyway.bigbang.framework.gray.IstioGrayRibbonRule;
import io.anyway.bigbang.framework.gray.KubernetesGrayRibbonRule;
import io.anyway.bigbang.framework.gray.NacosGrayRibbonRule;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.GenericFilterBean;

@Slf4j
@Configuration
@ConditionalOnDiscoveryEnabled
@ConditionalOnClass(GenericFilterBean.class)
@RibbonClients(defaultConfiguration = RibbonConfigure.class)
public class RibbonConfigure{

    @Autowired(required = false)
    private IClientConfig config;

    //TODO BlockingLoadBalancerClient
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