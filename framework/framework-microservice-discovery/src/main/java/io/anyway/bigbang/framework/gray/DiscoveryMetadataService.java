package io.anyway.bigbang.framework.gray;


import org.springframework.beans.factory.annotation.Value;

public abstract class DiscoveryMetadataService {

    @Value("${spring.application.name}")
    private String serviceId;

    @Value("${spring.cloud.nacos.discovery.cluster:DEFAULT}")
    private String group;

    @Value("${spring.cloud.nacos.discovery.metadata.version:}")
    private String version;

    public abstract String getIp();

    public abstract int getPort();

    final public String getServiceId(){
        return serviceId;
    }

    final public String getVersion(){
        return version;
    }

    final public String getGroup(){
        return group;
    }

}
