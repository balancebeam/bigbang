package io.anyway.bigbang.gateway.gray;

import io.anyway.bigbang.framework.gray.GrayContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class KubernetesGrayRibbonRuleImpl implements GrayRibbonRule{

    private AtomicInteger pos= new AtomicInteger(new Random().nextInt(1000));

    @Autowired
    private DiscoveryClient discoveryClient;

    @Override
    public Response<ServiceInstance> choose(String serviceId, List<ServiceInstance> instances, GrayContext ctx){
        List<ServiceInstance> availableInstances= discoveryClient.getInstances(getK8sServiceName(serviceId,ctx.getGroup()));
        if(CollectionUtils.isEmpty(availableInstances)){
            availableInstances= discoveryClient.getInstances(getK8sServiceName(serviceId,ctx.getDefGroup()));
        }
        if(!CollectionUtils.isEmpty(availableInstances)){
            ServiceInstance instance= availableInstances.get(Math.abs(pos.incrementAndGet()) % availableInstances.size());
            return new DefaultResponse(instance);
        }
        log.warn("cannot find appropriate match candidate server: {}",ctx.toString());
        return new EmptyResponse();
    }

    private String getK8sServiceName(String serviceId,String group){
        if(StringUtils.isEmpty(group)){
            return serviceId;
        }
        return serviceId+ "-"+group;
    }
}