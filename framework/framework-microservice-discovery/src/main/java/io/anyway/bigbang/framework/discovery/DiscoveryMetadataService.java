package io.anyway.bigbang.framework.discovery;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public abstract class DiscoveryMetadataService implements InitializingBean {

    @Value("${spring.application.name}")
    private String serviceId;

    @Value("${spring.application.version}")
    private String version;

    @Value("${server.port:8080}")
    private int port;

    public abstract String getIp();

    public int getPort(){
        return port;
    }

    @Override
    public void afterPropertiesSet() throws Exception{
        log.info("application name: {}, version: {}, ip: {}, port: {}"
                ,getServiceId(),getVersion(),getIp(),getPort());
    }

    final public String getServiceId(){
        return serviceId;
    }

    final public String getVersion(){
        return version;
    }

}
