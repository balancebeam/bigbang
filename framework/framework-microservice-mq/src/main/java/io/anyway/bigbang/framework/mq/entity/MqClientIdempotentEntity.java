package io.anyway.bigbang.framework.mq.entity;

import io.anyway.bigbang.framework.model.entity.AbstractEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.Date;


@Getter
@Setter
@ToString(callSuper = true)
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MqClientIdempotentEntity extends AbstractEntity {
    /**
     * 消息唯一Id
     */
    String messageId;
    /**
     * 消息中间件类型
     */
    String mqType;
    /**
     * 业务流水号
     */
    String transactionId;
    /**
     * 消息的目标地址
     */
    String destination;
    /**
     * 消息体信息
     */
    String messageHeader;
    /**
     * 消息体内容json结构
     */
    String messageBody;
    /**
     * 消息标签
     */
    String tags;
    /**
     * 消息发送可选项
     */
    String attribute;
}