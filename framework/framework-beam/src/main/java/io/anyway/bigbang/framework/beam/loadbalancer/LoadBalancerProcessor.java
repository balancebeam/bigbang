package io.anyway.bigbang.framework.beam.loadbalancer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
public class LoadBalancerProcessor implements BeanPostProcessor{

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if(bean instanceof LoadBalancerInterceptor) {
			log.info("delegate LoadBalancerInterceptor: {}",bean);
			return new XLoadBalancerInterceptor((LoadBalancerInterceptor)bean);
		}
		return bean;
	}

}
