package io.anyway.bigbang.framework.discovery;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GrayRouteContext {

    final public static String GRAY_CLUSTER_NAME= "x-gray-cluster";

    private String value;
    private String indicator;
    private String defaultValue;
}
