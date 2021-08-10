package io.anyway.bigbang.framework.autoconfigure;

import io.anyway.bigbang.framework.apm.config.LocalTraceConfigure;
import io.anyway.bigbang.framework.bootstrap.config.BootstrapConfigure;
import io.anyway.bigbang.framework.exception.config.ExceptionConfigure;
import io.anyway.bigbang.framework.header.config.HeaderConfigure;
import io.anyway.bigbang.framework.executor.config.ExecutionConfigure;
import io.anyway.bigbang.framework.discovery.config.DiscoveryConfigure;
import io.anyway.bigbang.framework.gray.config.GrayConfigure;
import io.anyway.bigbang.framework.interceptor.config.InterceptorConfigure;
import io.anyway.bigbang.framework.logging.config.LoggingConfigure;
import io.anyway.bigbang.framework.metrics.config.MetricsConfigure;
import io.anyway.bigbang.framework.security.config.SecurityConfigure;
import io.anyway.bigbang.framework.session.config.SessionConfigure;
import io.anyway.bigbang.framework.swagger.config.Swagger2Configure;
import io.anyway.bigbang.framework.useragent.config.UserAgentConfigure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Slf4j
@Configuration
@ConditionalOnClass(Filter.class)
@AutoConfigureBefore(TaskExecutionAutoConfiguration.class)
@ImportAutoConfiguration({
        InterceptorConfigure.class,
        DiscoveryConfigure.class,
        BootstrapConfigure.class,
        HeaderConfigure.class,
        LoggingConfigure.class,
        ExceptionConfigure.class,
        ExecutionConfigure.class,
        MetricsConfigure.class,
        SessionConfigure.class,
        SecurityConfigure.class,
        UserAgentConfigure.class,
        GrayConfigure.class,
        Swagger2Configure.class,
        LocalTraceConfigure.class
})
public class MicroserviceAutoConfigure {

}
