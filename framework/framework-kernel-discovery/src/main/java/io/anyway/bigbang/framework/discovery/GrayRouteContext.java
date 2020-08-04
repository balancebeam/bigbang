package io.anyway.bigbang.framework.discovery;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GrayRouteContext {
    final public static String GRAY_ROUTE_NAME = "x-gray-route";
    final public static String ATTRIBUTE_GROUP= "spring.cloud.nacos.discovery.group";
    final public static String ATTRIBUTE_CLUSTER_NAME= "spring.cloud.nacos.discovery.cluster-name";
    private String group;
    private String clusterName;
}
