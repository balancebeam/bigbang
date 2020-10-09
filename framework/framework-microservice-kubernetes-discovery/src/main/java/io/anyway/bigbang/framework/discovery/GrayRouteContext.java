package io.anyway.bigbang.framework.discovery;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GrayRouteContext {
    final public static String GRAY_ROUTE_NAME = "x-gray-route";
    final public static String ATTRIBUTE_CLUSTER_NAME= "nacos.cluster";
    private String cluster;
    private String defaultCluster;
}
