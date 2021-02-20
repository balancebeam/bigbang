package io.anyway.bigbang.framework.discovery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.commons.util.InetUtils;

public class KubernetesDiscoveryMetadataServiceImpl extends DiscoveryMetadataService {

    @Autowired
    private InetUtils inetUtils;

    @Override
    public String getIp() {
        return inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
    }

}
