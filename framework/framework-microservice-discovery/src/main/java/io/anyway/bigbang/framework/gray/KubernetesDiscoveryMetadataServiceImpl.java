package io.anyway.bigbang.framework.gray;

import io.anyway.bigbang.framework.utils.NetUtil;
import org.springframework.beans.factory.annotation.Value;

public class KubernetesDiscoveryMetadataServiceImpl extends DiscoveryMetadataService {


    @Value("${server.port:8080}")
    private int port;

    @Override
    public String getIp() {
        return NetUtil.getInet4Address().getHostName();
    }

    @Override
    public int getPort() {
        return port;
    }

}
