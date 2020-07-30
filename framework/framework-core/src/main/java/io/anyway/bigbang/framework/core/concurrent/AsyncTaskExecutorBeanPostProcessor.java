package io.anyway.bigbang.framework.core.concurrent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.task.AsyncTaskExecutor;

@Slf4j
public class AsyncTaskExecutorBeanPostProcessor implements BeanPostProcessor, PriorityOrdered {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean instanceof AsyncTaskExecutor){
            log.info("wrap the AsyncTaskExecutor: {}",bean);
            return new AsyncTaskExecutorWrapper((AsyncTaskExecutor)bean);
        }
        return bean;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
