package io.anyway.bigbang.framework.gray;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
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

}
