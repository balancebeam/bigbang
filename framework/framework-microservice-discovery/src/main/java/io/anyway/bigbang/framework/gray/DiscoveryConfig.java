package io.anyway.bigbang.framework.gray;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.tianrang.framework.kernel.discovery.DiscoveryMetadataService;
import com.tianrang.framework.kernel.discovery.impl.KubernetesDiscoveryMetadataServiceImpl;
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
    @ConditionalOnKubernetesDiscoveryEnabled
    public DiscoveryMetadataService createK8SDiscoveryMetadataService(){
        return new KubernetesDiscoveryMetadataServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public DiscoveryMetadataService createNoneDiscoveryMetadataService(){
        return new KubernetesDiscoveryMetadataServiceImpl();
    }

}
