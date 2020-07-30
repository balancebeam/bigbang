package io.anyway.bigbang.framework.core.autoconfigure;

import io.anyway.bigbang.framework.core.concurrent.AsyncTaskExecutorBeanPostProcessor;
import io.anyway.bigbang.framework.core.concurrent.ConcurrentWrapper;
import io.anyway.bigbang.framework.core.exception.ControllerExceptionAdvice;
import io.anyway.bigbang.framework.core.interceptor.HeaderDeliveryService;
import io.anyway.bigbang.framework.core.logging.controller.LogManagementController;
import io.anyway.bigbang.framework.core.rest.RestTemplateConfig;
import io.anyway.bigbang.framework.core.security.SecurityContextConfig;
import io.anyway.bigbang.framework.core.system.InstanceConfigBean;
import io.anyway.bigbang.framework.core.trace.FrameworkTraceContextConfig;
import io.anyway.bigbang.framework.core.utils.IdGenerator;
import io.anyway.bigbang.framework.core.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@ImportAutoConfiguration({
        RestTemplateConfig.class,
        ConcurrentWrapper.class,
        SecurityContextConfig.class,
        FrameworkTraceContextConfig.class,
        IdGenerator.class,
        SpringUtils.class
})
public class PlatformEnvironmentConfiguration {

    @Value("${server.port:8000}")
    private int port;

    @Value("${spring.bigbang.executor.core-pool-size:10}")
    private int corePoolSize;

    @Value("${spring.bigbang.executor.max-pool-size:30}")
    private int maxPoolSize;

    @Value("${spring.bigbang.executor.queue-capacity:50}")
    private int queueCapacity;

    @Value("${spring.bigbang.executor.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Bean("taskExecutor")
    public AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadGroupName("taskExecutor");
        executor.setThreadNamePrefix("taskExecutor-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.initialize();
        return executor;
    }

    @Bean
    @ConditionalOnMissingBean
    public InstanceConfigBean createDefaultInstanceConfigBean(){
        InstanceConfigBean instanceConfigBean= new InstanceConfigBean();
        String host= "127.0.0.1";
        try {
            host = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("{}",e.getMessage());
        }
        instanceConfigBean.setHost(host);
        instanceConfigBean.setPort(port);
        return instanceConfigBean;
    }

    @Bean
    public AsyncTaskExecutorBeanPostProcessor createAsyncTaskExecutorBeanPostProcessor(){
        return new AsyncTaskExecutorBeanPostProcessor();
    }

    @Bean
    public HeaderDeliveryService createHeaderDeliveryService(){
        return new HeaderDeliveryService();
    }

    @Bean
    public ControllerExceptionAdvice createControllerExceptionAdvice(){
        return new ControllerExceptionAdvice();
    }

    @Bean
    public LogManagementController createLogManagementController(){
        return new LogManagementController();
    }
}
