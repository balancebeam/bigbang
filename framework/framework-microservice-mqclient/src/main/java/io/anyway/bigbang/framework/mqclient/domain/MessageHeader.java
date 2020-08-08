package io.anyway.bigbang.framework.mqclient.domain;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MessageHeader {
    private String serviceId;
    private String group;
    private String clusterName;
    private String ip;
    private int persistMode;
    private String platform;
    private String version;
    private String traceId;
}
