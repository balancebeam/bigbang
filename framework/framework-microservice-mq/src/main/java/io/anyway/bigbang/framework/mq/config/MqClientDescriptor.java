package io.anyway.bigbang.framework.mq.config;

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
public class MqClientDescriptor {
    /**
     * 服务地址
     */
    String serverAddress;
    /**
     * 发送方配置
     */
    Map<String,Object> producer = Collections.EMPTY_MAP;
    /**
     * 消息方配置
     */
    Map<String,Object> consumer = Collections.EMPTY_MAP;

}
