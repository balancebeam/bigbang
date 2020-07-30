package io.anyway.bigbang.framework.kernel.concurrent;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableConfigurationProperties(TreadPoolTaskExecutorProperties.class)
public class ThreadPoolTaskExecutorConfig {

    @Resource
    private TreadPoolTaskExecutorProperties properties;

    @Bean("taskExecutor")
    public InheritableThreadPoolTaskExecutor threadPoolTaskExecutor(){
        InheritableThreadPoolTaskExecutor executor  = new InheritableThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());//核心池大小
        executor.setMaxPoolSize(properties.getMaxPoolSize());//最大线程数
        executor.setQueueCapacity(properties.getQueueCapacity());//队列程度
        executor.setKeepAliveSeconds(properties.getKeepAliveSeconds());//线程空闲时间
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());//线程前缀名称
        executor.setThreadGroupName(properties.getThreadGroupName());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());//配置拒绝策略
        return executor;
    }
}
