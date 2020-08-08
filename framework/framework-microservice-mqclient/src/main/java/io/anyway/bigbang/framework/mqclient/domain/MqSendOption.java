package io.anyway.bigbang.framework.mqclient.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

//import javax.validation.constraints.NotNull;


@Setter
@Getter
@ToString
public class MqSendOption {

    //    @NotNull(message = "bizId cannot be null!")
    private String bizId = "bizId";

    //    @NotNull(message = "bizType cannot be null!")
    private String bizType = "bizType";

    //    @NotNull(message = "referenceId cannot be null!")
    private String referenceId = UUID.randomUUID().toString();

    //    @NotNull(message = "messageType cannot be null!")
    private MessageType messageType = MessageType.TOPIC;

    //    @NotNull(message = "message cannot be null!")
    private String message;

    //    @NotNull(message = "destination cannot be null!")
    private String destination;

    private String messageKey;

    private String tags;

    private String sendOpts;

    private MqType mqType = MqType.RMQ;

    // Option to persist the message in local client db
    private MessagePersistModeStatus persistMode = MessagePersistModeStatus.BURN_AFTER_SENT;

    // Option to have the feature have idempotent control, if will it is true, message should be provided
    private boolean idempotentOn = false;

    // To indicate whether the sending is in failover mode
    private boolean failOverContext = false;
}
