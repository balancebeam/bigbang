package io.anyway.bigbang.framework.mq.constant;


import io.anyway.bigbang.framework.model.enumeration.EnumStatement;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum MqTypeEnum implements EnumStatement {
    ACTIVEMQ("activemq","activemq"),
    RABBITMQ("rabbitmq","rabbitmq"),
    ROCKETMQ("rocketmq","rocketmq"),
    KAFKA("kafka","kafka"),
    PULSAR("pulsar","pulsar"),
    DEFAULT("default","default");

    /**
     * 消息中间件类型
     */
    @JsonValue
    String code;
    /**
     * 中文描述
     */
    String message;
}

