package io.anyway.bigbang.framework.kernel.config;

import io.anyway.bigbang.framework.kernel.exception.FeignClientErrorDecoder;
import io.anyway.bigbang.framework.kernel.exception.GlobalExceptionHandler;
import io.anyway.bigbang.framework.kernel.gray.GrayConfig;
import io.anyway.bigbang.framework.kernel.header.headerConfig;
import io.anyway.bigbang.framework.kernel.metrics.PlatformMetricConfig;
import io.anyway.bigbang.framework.kernel.security.SecurityConfig;
import io.anyway.bigbang.framework.kernel.swagger.Swagger2Config;
import io.anyway.bigbang.framework.kernel.task.TransmittableTaskExecutionConfig;
import io.anyway.bigbang.framework.kernel.useragent.UserAgentConfig;
import io.anyway.bigbang.framework.kernel.utils.IdGenerator;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureBefore(TaskExecutionAutoConfiguration.class)
@ImportAutoConfiguration({
        FeignClientErrorDecoder.class,
        TransmittableTaskExecutionConfig.class,
        PlatformMetricConfig.class,
        headerConfig.class,
        SecurityConfig.class,
        GrayConfig.class,
        UserAgentConfig.class,
        GlobalExceptionHandler.class,
        Swagger2Config.class,
        IdGenerator.class
})
public class KernelConfig {
}
