package io.anyway.bigbang.framework.interceptor.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

@Slf4j
public class NoRegisterFeignClientProcessor implements BeanDefinitionRegistryPostProcessor , EnvironmentAware {

    private Environment environment;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        for(String beanName: registry.getBeanDefinitionNames()){
            BeanDefinition definition= registry.getBeanDefinition(beanName);
            String clsName= definition.getBeanClassName();
            if(clsName== null){
                continue;
            }
            if("org.springframework.cloud.openfeign.FeignClientFactoryBean".equals(clsName)){
                PropertyValue propertyValue= definition.getPropertyValues().getPropertyValue("name");
                if(propertyValue!= null){
                    String appId= propertyValue.getValue().toString();
                    if(environment.getProperty("spring.application.name").equals(appId)){
                        log.info("didn't register feign client: {}",beanName);
                        registry.removeBeanDefinition(beanName);
                    }
                }
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException { }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment= environment;
    }
}
