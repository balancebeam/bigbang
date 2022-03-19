package io.anyway.bigbang.framework.mq.domain;

import io.anyway.bigbang.framework.mq.constant.MessageStateEnum;
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
public class MessageProducerOutbound {
    /**
     * 消息发送返回状态
     */
    MessageStateEnum state;
    /**
     * 消息Id
     */
    String messageId;
    /**
     * 消息发送失败原因
     */
    String cause;
}
