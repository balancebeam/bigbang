package io.anyway.bigbang.framework.beam.service;

import com.netflix.loadbalancer.Server;

import java.util.List;

public interface WeightBalancerService {

    Server choose(String serviceId,List<Server> servers);
}
