package io.anyway.bigbang.framework.gray;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;

import javax.annotation.Resource;

public class NacosDiscoveryMetadataServiceImpl extends DiscoveryMetadataService {

    @Resource
    private NacosDiscoveryProperties props;

    @Override
    public String getIp() {
        return props.getIp();
    }

    @Override
    public int getPort() {
        return props.getPort();
    }

}
