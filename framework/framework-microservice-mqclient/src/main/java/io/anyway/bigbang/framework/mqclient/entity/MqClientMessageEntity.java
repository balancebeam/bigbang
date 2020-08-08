package io.anyway.bigbang.framework.mqclient.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
public class MqClientMessageEntity {
    private Long id;
    private String bizId;
    private String bizType;
    private String message;
    private String messageType;
    private String destination;
    private String tags;
    private String messageKey;
    private String status;
    private Date nextRetryAt;
    private Integer retryCount;
    private String failureReason;
    private String messageId;
    private String sendOpts;
    private Date createdAt;
    private Date updatedAt;
    private Long partitionKey;
    private String mqType;
    private String messageHeader;
    private int persistMode;
}
