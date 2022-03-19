package io.anyway.bigbang.framework.mq.domain;

import io.anyway.bigbang.framework.gray.GrayContext;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageHeader {
    /**
     * 业务流水号
     */
    String transactionId;
    /**
     * 消息发起方的服务Id
     */
    String serviceId;
    /**
     * 消息发起方ip地址
     */
    String ip;
    /**
     * 服务版本
     */
    String version;
    /**
     * 跟踪链信息
     */
    String traceId;
    /**
     * 灰度信息
     */
    GrayContext grayContext;
    /**
     * 消息持久化模式
     */
    String persistMode;

}
