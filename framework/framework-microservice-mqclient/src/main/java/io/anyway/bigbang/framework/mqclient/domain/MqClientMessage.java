package io.anyway.bigbang.framework.mqclient.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Setter
@Getter
@ToString
public class MqClientMessage {
    private String messageId;
    private String messageKey;
    private String message;
    private String messageType;
    private String destination;
    private String tags = "";

    private MessageHeader messageHeader;
}
