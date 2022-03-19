package io.anyway.bigbang.framework.mq.domain;

import io.anyway.bigbang.framework.mq.constant.MqTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageListenerInbound {
    /**
     * 消息中间件类型
     */
    MqTypeEnum mqType;
    /**
     * 消息体信息
     */
    MessageHeader messageHeader;
    /**
     * 消息体内容json结构
     */
    String messageBody;
    /**
     * 业务流水号
     */
    String transactionId;
    /**
     * 消息id
     */
    String messageId;
    /**
     * 标识
     */
    String tags = "";
    /**
     * 消息地址
     */
    String destination;
    /**
     * 其他属性
     */
    Map<String,Object> attribute= new LinkedHashMap<>();
}
