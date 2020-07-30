package io.anyway.bigbang.framework.beam.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class UnitRouterStrategy implements Serializable {
    private String id;
    private String sourceServiceId;
    private String sourceServiceUnit;
    private String targetServiceId;
    private String targetServiceUnit;
}
