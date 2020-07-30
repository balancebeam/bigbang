package io.anyway.bigbang.framework.beam.service.impl;

import com.netflix.loadbalancer.Server;
import io.anyway.bigbang.framework.beam.service.NodeMetadataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;

import java.util.Map;

@Slf4j
public class EurekaMetadataServiceImpl implements NodeMetadataService {

    @Override
    public String getValue(Server server, String name) {
        Map<String, String> metadata = ((DiscoveryEnabledServer) server).getInstanceInfo().getMetadata();
		return metadata.get(name);
    }
}
