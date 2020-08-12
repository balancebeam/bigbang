package io.anyway.bigbang.framework.mqclient.domain;

import io.anyway.bigbang.framework.discovery.GrayRouteContext;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MessageHeader {
    private String serviceId;
    private GrayRouteContext grayRouteContext;
    private String ip;
    private int persistMode;
    private String platform;
    private String version;
    private String traceId;
}
