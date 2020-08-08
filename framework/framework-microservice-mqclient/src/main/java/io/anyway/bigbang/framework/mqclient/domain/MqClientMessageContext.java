package io.anyway.bigbang.framework.mqclient.domain;

import io.anyway.bigbang.framework.mqclient.entity.MqClientMessageEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class MqClientMessageContext {
    private MqClientMessageEntity mqClientMessageEntity;
    private MessagePersistModeStatus persistMode;
    private boolean failoverCtx = false;
}
