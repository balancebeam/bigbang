package io.anyway.bigbang.framework.core.system;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InstanceConfigBean {
    private String host;
    private int port;

    public String getHostPort(){
        return host+":"+port;
    }
}
