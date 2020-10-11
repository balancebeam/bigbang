package io.anyway.bigbang.framework.autoconfigure;

import io.anyway.bigbang.framework.exception.FeignClientErrorDecoder;
import io.anyway.bigbang.framework.exception.GlobalExceptionHandler;
import io.anyway.bigbang.framework.executor.TransmittableTaskExecutionConfig;
import io.anyway.bigbang.framework.gray.DiscoveryConfig;
import io.anyway.bigbang.framework.gray.GrayConfig;
import io.anyway.bigbang.framework.interceptor.InterceptorConfig;
import io.anyway.bigbang.framework.logging.marker.LoggingMarkerAspect;
import io.anyway.bigbang.framework.metrics.FrameworkMetricsConfig;
import io.anyway.bigbang.framework.swagger.Swagger2Config;
import io.anyway.bigbang.framework.useragent.UserAgentContextConfig;
import io.anyway.bigbang.framework.utils.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.security.SecurityConfig;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@AutoConfigureBefore(TaskExecutionAutoConfiguration.class)
@ImportAutoConfiguration({
        InterceptorConfig.class,
        DiscoveryConfig.class,
        LoggingMarkerAspect.class,
        FeignClientErrorDecoder.class,
        TransmittableTaskExecutionConfig.class,
        FrameworkMetricsConfig.class,
        SecurityConfig.class,
        UserAgentContextConfig.class,
        GlobalExceptionHandler.class,
        GrayConfig.class,
        Swagger2Config.class,
        IdGenerator.class
})
public class MicroserviceConfig {

}
