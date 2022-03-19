package io.anyway.bigbang.framework.mq.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.Method;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageListenerInvoker {
    /**
     * 对象实例
     */
    Object instance;
    /**
     * 消息监听方法
     */
    Method method;
    /**
     * 消息监听入参
     */
    Class parameterType;
}
