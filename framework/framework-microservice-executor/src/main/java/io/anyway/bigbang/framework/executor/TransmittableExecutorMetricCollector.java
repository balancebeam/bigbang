package io.anyway.bigbang.framework.executor;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public class TransmittableExecutorMetricCollector implements MeterBinder {

    @Autowired(required = false)
    private List<Executor> executors = Collections.EMPTY_LIST;

    @Override
    public void bindTo(MeterRegistry registry) {
        for (Executor each : executors) {
            if (each instanceof ThreadPoolTaskExecutor) {
                ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) each;
                Field beanNameField= ReflectionUtils.findField(ExecutorConfigurationSupport.class,"beanName",String.class);
                ReflectionUtils.makeAccessible(beanNameField);
                String name= (String)ReflectionUtils.getField(beanNameField,each);
                String prefix= "executor."+name+".";
                Gauge.builder(prefix+"active_count", executor, x -> executor.getActiveCount()).register(registry);
                Gauge.builder(prefix+"max_active_count", executor, x -> executor.getMaxPoolSize()).register(registry);
                Gauge.builder(prefix+"task_count", executor, x -> executor.getThreadPoolExecutor().getTaskCount()).register(registry);
            }
        }
    }
}

