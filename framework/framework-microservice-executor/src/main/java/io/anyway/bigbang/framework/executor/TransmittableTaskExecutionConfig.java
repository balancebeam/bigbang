package io.anyway.bigbang.framework.executor;

import org.springframework.boot.task.TaskExecutorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.AsyncAnnotationBeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME;

@Configuration
public class TransmittableTaskExecutionConfig {

    @Bean
    public TransmittableExecutorMetricCollector createTransmittableExecutorMetricCollector(){
        return new TransmittableExecutorMetricCollector();
    }

    @Lazy
    @Bean(name = { APPLICATION_TASK_EXECUTOR_BEAN_NAME,
            AsyncAnnotationBeanPostProcessor.DEFAULT_TASK_EXECUTOR_BEAN_NAME })
    public ThreadPoolTaskExecutor createTransmittableTaskExecutor(TaskExecutorBuilder builder) {
        return builder.build(TransmittableTaskExecutor.class);
    }
}
