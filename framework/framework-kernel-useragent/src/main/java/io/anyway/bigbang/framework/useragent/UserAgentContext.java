package io.anyway.bigbang.framework.useragent;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class UserAgentContext {

    final public static String USER_AGENT_NAME= "x-user-agent";

    private String version = "v1.0.0-snapshot";; //App version
    private String platform= "browser"; //ISO or Android
    private String os; //Operation system
    private String screen;
    private String model;
    private String channel;
    private String net; //wifi
    private String appId; //package
    private String deviceId; //equipment uniquely identifies
    private String unit;
    private String locationLatitude;
    private String locationLongitude;
}
