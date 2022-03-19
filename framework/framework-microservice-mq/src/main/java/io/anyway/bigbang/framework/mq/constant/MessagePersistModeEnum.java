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
public enum MessagePersistModeEnum implements EnumStatement<String> {
    KEEP_AFTER_SENT("keep","消息发送后保留"),
    BURN_AFTER_SENT("burn","消息阅后即焚"),
    BURN_BEFORE_SEND("none","消息不保存");
    /**
     * 消息保存状态
     */
    @JsonValue
    String code;
    /**
     * 中文描述
     */
    String message;
}
