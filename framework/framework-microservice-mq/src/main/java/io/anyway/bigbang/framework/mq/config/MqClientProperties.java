package io.anyway.bigbang.framework.mq.config;

import io.anyway.bigbang.framework.mq.constant.MqTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.Map;


@Getter
@Setter
@ToString
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ConfigurationProperties(prefix = "spring.reliable-mq")
public class MqClientProperties {

    int workQueueCorePoolSize = 5;
    int workQueueMaxPoolSize = 5;
    int workQueueCapacity = 2000;
    int workQueueKeepAliveSeconds = 300;
    /**
     * 消息中间件默认类型
     */
    MqTypeEnum mqType = MqTypeEnum.ROCKETMQ;
    /**
     * 消息客户端配置
     */
    Map<String,MqClientDescriptor> client = Collections.EMPTY_MAP;
    /**
     * 消息顺序执行
     */
    boolean msgListeningOrderly = false;
    /**
     * 消息消费幂等性
     */
    boolean idempotentOn= true;

}
