package io.anyway.bigbang.framework.mq.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageProducerInbound {
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
