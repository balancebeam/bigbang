package io.anyway.bigbang.framework.executor;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executor;

public class TransmittableExecutorMetricCollector implements MeterBinder {

    @Autowired(required = false)
    private Map<String,Executor> executorMap = Collections.emptyMap();

    @Override
    public void bindTo(MeterRegistry registry) {
        for (Map.Entry<String,Executor> each : executorMap.entrySet()) {
            if (each.getValue() instanceof ThreadPoolTaskExecutor) {
                ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) each.getValue();
                String prefix= "executor."+each.getKey()+".";
                Gauge.builder(prefix+"active_count", executor, x -> executor.getActiveCount()).register(registry);
                Gauge.builder(prefix+"max_active_count", executor, x -> executor.getMaxPoolSize()).register(registry);
                Gauge.builder(prefix+"task_count", executor, x -> executor.getThreadPoolExecutor().getTaskCount()).register(registry);
            }
        }
    }
}

