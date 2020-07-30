package io.anyway.bigbang.framework.kernel.gray;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@EnableConfigurationProperties(MetadataMapProperties.class)
public class NacosMetadataMapConfig {

    final public static String GRAY_UNIT_NAME = "x-gray-unit";

    final public static String GRAY_INDICATOR_NAME = "x-gray-indicator";

    final public static String GRAY_INDICATOR_DEFAULT_VALUE_NAME = "x-gray-indicator-default-value";

    final public static String GRAY_DEFAULT_UNIT= "blue";

    final public static String GRAY_DEFAULT_INDICATOR= "gray.unit";

    @Resource
    private MetadataMapProperties metadataMapProperties;

    @Bean
    public BeanPostProcessor createNacosDiscoveryPropertiesProcessor(){
        return new BeanPostProcessor(){
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if(bean instanceof NacosDiscoveryProperties){
                    NacosDiscoveryProperties nacosDiscoveryProperties= (NacosDiscoveryProperties)bean;
                    for(Map.Entry<String,String> each: metadataMapProperties.getMetadataMap().entrySet()){
                        nacosDiscoveryProperties.getMetadata().put(each.getKey(),each.getValue());
                    }
                }
                return bean;
            }
        };
    }
}
