package io.anyway.bigbang.framework.beam.service;

import com.netflix.loadbalancer.Server;

public interface NodeMetadataService {

    String getValue(Server server,String name);
}
