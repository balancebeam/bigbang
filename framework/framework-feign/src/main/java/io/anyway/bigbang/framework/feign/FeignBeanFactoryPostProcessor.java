package io.anyway.bigbang.framework.feign;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class FeignBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    private boolean containsBeanDefinition(ConfigurableListableBeanFactory beanFactory, String... beans) {
        return Arrays.stream(beans).allMatch(b -> beanFactory.containsBeanDefinition(b));
    }
    
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    	
        if (containsBeanDefinition(beanFactory, "feignContext", "eurekaAutoServiceRegistration")) {

            beanFactory.getBeanDefinition("feignContext").setDependsOn("eurekaAutoServiceRegistration");
        }
    }

}
