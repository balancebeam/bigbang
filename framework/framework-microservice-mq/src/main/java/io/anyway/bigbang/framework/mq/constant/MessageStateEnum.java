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
public enum MessageStateEnum implements EnumStatement {
    SUBMIT("Submit","提交"),
    SUCCESS("Success","成功"),
    FAILURE("Failure","失败"),
    EXCEPTION("Exception","异常");

    /**
     * 消息发送状态
     */
    @JsonValue
    String code;
    /**
     * 中文描述
     */
    String message;
}
