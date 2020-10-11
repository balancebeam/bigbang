package io.anyway.bigbang.framework.mqclient.domain;

import io.anyway.bigbang.framework.gray.GrayContext;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MessageHeader {
    private String serviceId;
    private GrayContext grayContext;
    private String ip;
    private int persistMode;
    private String platform;
    private String version;
    private String traceId;
}
