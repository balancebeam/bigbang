package io.anyway.bigbang.framework.discovery.config;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import io.anyway.bigbang.framework.discovery.DiscoveryMetadataService;
import io.anyway.bigbang.framework.discovery.KubernetesDiscoveryMetadataServiceImpl;
import io.anyway.bigbang.framework.discovery.NacosDiscoveryMetadataServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableDiscoveryClient
public class DiscoveryConfigure {

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
