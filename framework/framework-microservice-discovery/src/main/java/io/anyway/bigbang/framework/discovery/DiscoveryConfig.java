package io.anyway.bigbang.framework.discovery;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscoveryConfig {

    @Bean
    @ConditionalOnNacosDiscoveryEnabled
    public DiscoveryMetadataService createNacosDiscoveryMetadataService(){
        return new NacosDiscoveryMetadataServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public DiscoveryMetadataService createNoneDiscoveryMetadataService(){
        return new KubernetesDiscoveryMetadataServiceImpl();
    }

}
