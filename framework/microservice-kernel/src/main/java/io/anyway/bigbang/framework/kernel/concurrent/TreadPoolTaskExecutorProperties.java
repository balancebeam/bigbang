package io.anyway.bigbang.framework.kernel.concurrent;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@ConfigurationProperties(prefix = "spring.executor.thread-pool")
public class TreadPoolTaskExecutorProperties {
    private int corePoolSize= 5;
    private int maxPoolSize= 50;
    private int queueCapacity= 1000;
    private int keepAliveSeconds= 1000;
    private String threadNamePrefix= "task-";
    private String threadGroupName= "taskExecutorGroup";
}
