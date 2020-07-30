package io.anyway.bigbang.framework.core.client;

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class ClientAgent {
    private String version; //App version
    private String platform; //ISO or Android
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

    public String toJson(){
        return JSONObject.toJSONString(this);
    }
}
