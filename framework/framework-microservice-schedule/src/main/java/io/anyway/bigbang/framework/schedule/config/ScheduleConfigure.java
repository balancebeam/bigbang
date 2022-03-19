package io.anyway.bigbang.framework.schedule.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import com.xxl.job.core.util.NetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class ScheduleConfigure {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${spring.xxl-job.admin-addresses}")
    private String adminAddresses;

    @Value("${spring.xxl-job.access-token:}")
    private String accessToken;

    @Value("${spring.xxl-job.executor.port}")
    private int port;

    @Value("${spring.xxl-job.executor.log-path}")
    private String logPath;

    @Value("${spring.xxl-job.executor.log-retention-days:30}")
    private int logRetentionDays;

    private int getAppPort() {
        port = port > 0 ? port : NetUtil.findAvailablePort(9999);
        return port;
    }

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appName);
//        xxlJobSpringExecutor.setIp(nacosDiscoveryProperties.getIp());
        xxlJobSpringExecutor.setPort(getAppPort());
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);
        return xxlJobSpringExecutor;
    }
}
