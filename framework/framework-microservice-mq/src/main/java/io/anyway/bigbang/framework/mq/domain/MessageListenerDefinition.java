package io.anyway.bigbang.framework.mq.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageListenerDefinition {
    /**
     * 消息监听定义id
     */
    String id;
    /**
     * 消息监听地址
     */
    String destination;
    /**
     * 消息标识
     */
    List<String> tags= Collections.emptyList();
    /**
     * 消息分组信息
     */
    String group;
    /**
     * 消息执行体列表
     */
    List<MessageListenerInvoker> invokerList= new ArrayList<>();
}
