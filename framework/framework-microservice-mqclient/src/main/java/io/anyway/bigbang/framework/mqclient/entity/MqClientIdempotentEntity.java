package io.anyway.bigbang.framework.mqclient.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Setter
@Getter
@ToString
public class MqClientIdempotentEntity {
    private String mqClientIdempotentTableName;
    private Long id;
    private String messageId;
    private String messageKey;
    private String message;
    private String messageType;
    private String destination;
    private String tags;
    private Long partitionKey;
    private String mqType;
    private Date createdAt;
    private Date updatedAt;
}