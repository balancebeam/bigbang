package io.anyway.bigbang.gateway.config;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import io.anyway.bigbang.framework.gray.ConditionalOnKubernetesDiscoveryEnabled;
import io.anyway.bigbang.gateway.gray.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
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
    @ConditionalOnMissingBean({GrayLoadBalancerFilter.class})
    public GrayLoadBalancerFilter grayReactiveLoadBalancerClientFilter(
            LoadBalancerClientFactory clientFactory,
            LoadBalancerProperties properties) {
        return new GrayLoadBalancerFilter(clientFactory, properties);
    }

    @Configuration
    public static class GrayRouteConfig2 {

        @Value("${spring.gateway.gray-route.dataId:gateway-gray-route}")
        private String dataId;

        @Value("${spring.gateway.gray-route.group:DEFAULT_GROUP}")
        private String group;

        @Value("${spring.cloud.nacos.config.server-addr}")
        private String serverAddr;

        @Resource
        private GrayListener grayRouteListener;

        @PostConstruct
        public void grayRouteByNacosListener() {
            try {
                ConfigService configService = NacosFactory.createConfigService(serverAddr);

                // When the app startups, fetch gateway gray router information firstly.
                String configInfo = configService.getConfig(dataId, group, 5000);
                setGrayRoutePolicy(configInfo);

                // Add gateway router listener
                configService.addListener(dataId, group, new AbstractListener() {
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        setGrayRoutePolicy(configInfo);
                    }
                });
            } catch (NacosException e) {
                log.error("init route definition error", e);
            }
        }

        private synchronized void setGrayRoutePolicy(String text) {
            log.info("gateway gray route strategy: {}",text);
            GrayStrategy strategy = StringUtils.isEmpty(text)?
                    new GrayStrategy():
                    JSONObject.parseObject(text, GrayStrategy.class);
            grayRouteListener.onChange(strategy);
        }
    }

}
