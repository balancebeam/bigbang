package io.anyway.bigbang.gateway.config;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import io.anyway.bigbang.framework.discovery.ConditionalOnIstioDiscoveryEnabled;
import io.anyway.bigbang.framework.discovery.ConditionalOnKubernetesDiscoveryEnabled;
import io.anyway.bigbang.gateway.gray.*;
import io.anyway.bigbang.gateway.gray.impl.IstioGrayRibbonRuleImpl;
import io.anyway.bigbang.gateway.gray.impl.KubernetesGrayRibbonRuleImpl;
import io.anyway.bigbang.gateway.gray.impl.NacosGrayRibbonRuleImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@Configuration
@AutoConfigureAfter(GrayRouteConfig.GrayRouteConfig2.class)
public class GrayRouteConfig {

    @Bean
    @ConditionalOnNacosDiscoveryEnabled
    public GrayRibbonRule nacosGrayRibbonRule() {
        NacosGrayRibbonRuleImpl rule = new NacosGrayRibbonRuleImpl();
        return rule;
    }


    @Bean
    @ConditionalOnKubernetesDiscoveryEnabled
    public GrayRibbonRule k8sGrayRibbonRule() {
        KubernetesGrayRibbonRuleImpl rule = new KubernetesGrayRibbonRuleImpl();
        return rule;
    }

    @Bean
    @ConditionalOnIstioDiscoveryEnabled
    public GrayRibbonRule istioGrayRibbonRule() {
        IstioGrayRibbonRuleImpl rule = new IstioGrayRibbonRuleImpl();
        return rule;
    }

    @Bean
    public GrayStrategyProcessor createGrayStrategyProcessor(){
        return new GrayStrategyProcessor();
    }


    @Bean
    @ConditionalOnMissingBean({GrayLoadBalancerFilter.class})
    public GrayLoadBalancerFilter grayReactiveLoadBalancerClientFilter(
            LoadBalancerClientFactory clientFactory,
            LoadBalancerProperties properties) {
        return new GrayLoadBalancerFilter(clientFactory, properties);
    }

    @Configuration
    public static class GrayRouteConfig2 {

        @Value("${spring.gateway.gray.dataId:gateway-gray-strategy}")
        private String dataId;

        @Value("${spring.gateway.gray.group:DEFAULT_GROUP}")
        private String group;

        @Value("${spring.cloud.nacos.config.server-addr}")
        private String serverAddr;

        @Resource
        private GrayStrategyListener grayStrategyListener;

        @PostConstruct
        public void grayStrategyByNacosListener() {
            try {
                ConfigService configService = NacosFactory.createConfigService(serverAddr);

                // When the app startups, fetch gateway gray router information firstly.
                String configInfo = configService.getConfig(dataId, group, 5000);
                setGrayStrategy(configInfo);

                // Add gateway router listener
                configService.addListener(dataId, group, new AbstractListener() {
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        setGrayStrategy(configInfo);
                    }
                });
            } catch (NacosException e) {
                log.error("init gray strategy definition error", e);
            }
        }

        private synchronized void setGrayStrategy(String text) {
            log.info("gateway gray strategy: {}",text);
            grayStrategyListener.onChangeEvent(text);
        }
    }

}
