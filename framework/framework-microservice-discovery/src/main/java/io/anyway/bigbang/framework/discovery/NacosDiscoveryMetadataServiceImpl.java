package io.anyway.bigbang.framework.discovery;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;

import javax.annotation.Resource;

public class NacosDiscoveryMetadataServiceImpl extends DiscoveryMetadataService {

    @Resource
    private NacosDiscoveryProperties props;

    @Override
    public void loadIpAddress() {
        ip= props.getIp();
    }

}

