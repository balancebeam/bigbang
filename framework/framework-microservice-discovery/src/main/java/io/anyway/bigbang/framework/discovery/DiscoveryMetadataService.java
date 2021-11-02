package io.anyway.bigbang.framework.discovery;


import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Getter
@ToString
public abstract class DiscoveryMetadataService implements InitializingBean {

    @Value("${spring.application.name}")
    private String serviceId;

    @Value("${spring.application.version:}")
    private String version;

    @Value("${spring.application.tag:}")
    private String tag;

    @Value("${spring.cloud.discovery.platform:}")
    private String platform;

    @Value("${server.port:8080}")
    private int port;

    protected String ip;

    protected abstract void loadIpAddress();

    final public void afterPropertiesSet() throws Exception{
        loadIpAddress();
        log.info("{}",this);
    }

}

