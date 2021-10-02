package io.anyway.bigbang.gateway.config;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import io.anyway.bigbang.framework.discovery.ConditionalOnIstioDiscoveryEnabled;
import io.anyway.bigbang.framework.discovery.ConditionalOnKubernetesDiscoveryEnabled;
import io.anyway.bigbang.gateway.filter.GrayLoadBalancerGlobalFilter;
import io.anyway.bigbang.gateway.gray.GrayRibbonRule;
import io.anyway.bigbang.gateway.gray.GrayStrategyEvent;
import io.anyway.bigbang.gateway.gray.GrayStrategyProcessor;
import io.anyway.bigbang.gateway.gray.impl.IstioGrayRibbonRuleImpl;
import io.anyway.bigbang.gateway.gray.impl.KubernetesGrayRibbonRuleImpl;
import io.anyway.bigbang.gateway.gray.impl.NacosGrayRibbonRuleImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Properties;

@Slf4j
@Configuration
public class GrayRouteConfig implements SmartInitializingSingleton {

    @Value("${spring.cloud.gateway.gray.dataId:gateway-gray-strategy}")
    private String dataId;

    @Resource
    private NacosConfigProperties nacosConfigProperties;

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
    @ConditionalOnMissingBean({GrayLoadBalancerGlobalFilter.class})
    public GrayLoadBalancerGlobalFilter grayReactiveLoadBalancerClientFilter(
            LoadBalancerClientFactory clientFactory,
            LoadBalancerProperties properties) {
        return new GrayLoadBalancerGlobalFilter(clientFactory, properties);
    }

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;


    @Override
    public void afterSingletonsInstantiated() {
        try {
            Properties properties = new Properties();
            if(!StringUtils.isEmpty(nacosConfigProperties.getNamespace())){
                properties.put(PropertyKeyConst.NAMESPACE, nacosConfigProperties.getNamespace());
            }
            properties.put(PropertyKeyConst.SERVER_ADDR, nacosConfigProperties.getServerAddr());
            properties.put("fileExtension","json");
            if(!StringUtils.isEmpty(nacosConfigProperties.getUsername())){
                properties.put(PropertyKeyConst.USERNAME,nacosConfigProperties.getUsername());
                properties.put(PropertyKeyConst.PASSWORD,nacosConfigProperties.getPassword());
            }

            ConfigService configService = NacosFactory.createConfigService(properties);
            // When the app startups, fetch gateway gray router information firstly.
            String configInfo =configService.getConfigAndSignListener(dataId, nacosConfigProperties.getGroup(), 5000, new AbstractListener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    if(StringUtils.isEmpty(configInfo)){
                        configInfo= "{}";
                    }
                    applicationEventPublisher.publishEvent(new GrayStrategyEvent(configInfo));
                }
            });
            if(StringUtils.isEmpty(configInfo)){
                configInfo= "{}";
            }
            applicationEventPublisher.publishEvent(new GrayStrategyEvent(configInfo));
        } catch (NacosException e) {
            log.error("init gray strategy definition error", e);
        }
    }
}
