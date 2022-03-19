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
public enum MessageTypeEnum implements EnumStatement<String> {
    TOPIC("topic","广播"),
    QUEUE("queue","队列");

    /**
     * 消息队列类型
     */
    @JsonValue
    String code;
    /**
     * 中文描述
     */
    String message;
}
