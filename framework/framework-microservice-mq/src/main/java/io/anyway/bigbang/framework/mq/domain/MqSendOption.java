package io.anyway.bigbang.framework.mq.domain;

import io.anyway.bigbang.framework.mq.constant.MessagePersistModeEnum;
import io.anyway.bigbang.framework.mq.constant.MessageTypeEnum;
import io.anyway.bigbang.framework.mq.constant.MqTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Collections;
import java.util.Map;


@Getter
@Setter
@ToString
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MqSendOption<T> {
    /**
     * 业务流水号
     */
    String transactionId = "bizId";
    /**
     * 业务类型
     */
    String transactionType = "bizType";
    /**
     * 消息类型
     */
    MessageTypeEnum messageType = MessageTypeEnum.TOPIC;
    /**
     * 消息内容体
     */
    T message;
    /**
     * 消息目标地址
     */
    String destination;
    /**
     * 消息标签
     */
    String tags= "";
    /**
     * 消息额外属性
     */
    Map<String,Object> attribute= Collections.emptyMap();

    /**
     * 消息中间件类型
     */
    MqTypeEnum mqType;
    /**
     * 发送消息后的持久化类型
     */
    MessagePersistModeEnum persistMode = MessagePersistModeEnum.BURN_AFTER_SENT;

}
