package io.anyway.bigbang.framework.metrics;

import io.anyway.bigbang.framework.core.concurrent.AsyncTaskExecutorWrapper;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;
import java.util.List;

@Slf4j
public class ExecutorThreadMetricCollector implements MeterBinder {

    @Autowired(required = false)
    private List<AsyncTaskExecutor> executors = Collections.EMPTY_LIST;

    @Override
    public void bindTo(MeterRegistry registry) {
        for (AsyncTaskExecutor each : executors) {
            if (each instanceof AsyncTaskExecutorWrapper) {
                Object delegate = ((AsyncTaskExecutorWrapper)each).getTarget();
                if (delegate instanceof ThreadPoolTaskExecutor) {
                    ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) delegate;
                    String name= executor.getThreadNamePrefix();
                    if(executor.getThreadGroup()!= null){
                        name= executor.getThreadGroup().getName();
                    }
                    String prefix= "bigbang.executor."+name+".";
                    Gauge.builder(prefix+"active_count", executor, x -> executor.getActiveCount()).register(registry);
                    Gauge.builder(prefix+"max_active_count", executor, x -> executor.getMaxPoolSize()).register(registry);
                    Gauge.builder(prefix+"task_count", executor, x -> executor.getThreadPoolExecutor().getTaskCount()).register(registry);
                }
            }
        }
    }
}
