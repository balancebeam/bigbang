package io.anyway.bigbang.gateway.gray;

import io.anyway.bigbang.framework.gray.GrayContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Response;

import java.util.*;

@Slf4j
public class IstioGrayRibbonRuleImpl implements GrayRibbonRule{

    @Override
    public Response<ServiceInstance> choose(String serviceId, List<ServiceInstance> instances, Optional<GrayContext> optional){
        return new DefaultResponse(new DefaultServiceInstance(serviceId,serviceId,serviceId,80,false));
    }

}