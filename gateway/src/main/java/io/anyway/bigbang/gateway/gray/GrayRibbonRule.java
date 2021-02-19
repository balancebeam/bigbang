package io.anyway.bigbang.gateway.gray;

import io.anyway.bigbang.framework.gray.GrayContext;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.Response;

import java.util.List;
import java.util.Optional;

public interface GrayRibbonRule{

    Response<ServiceInstance> choose(String serviceId, List<ServiceInstance> instances, Optional<GrayContext> optional);

}