package io.anyway.bigbang.framework.discovery;

import org.springframework.cloud.commons.util.InetUtils;

import javax.annotation.Resource;

public class KubernetesDiscoveryMetadataServiceImpl extends DiscoveryMetadataService {

    @Resource
    private InetUtils inetUtils;

    @Override
    public void loadIpAddress() {
        ip= inetUtils.findFirstNonLoopbackHostInfo().getIpAddress();
    }
}
