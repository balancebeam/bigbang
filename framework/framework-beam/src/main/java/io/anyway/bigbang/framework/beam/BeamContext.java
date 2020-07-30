package io.anyway.bigbang.framework.beam;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class BeamContext {

    private String uid;
    private String unit;
    private String platform;
    private String version;
    private String clientId;
    private String sourceServiceId;
    private String sourceServiceUnit;

}
