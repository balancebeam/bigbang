package io.anyway.bigbang.gateway.gray.impl;

import io.anyway.bigbang.framework.gray.GrayContext;
import io.anyway.bigbang.gateway.gray.GrayRibbonRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Slf4j
public class NacosGrayRibbonRuleImpl implements GrayRibbonRule {

    private AtomicInteger position= new AtomicInteger(new Random().nextInt(1000));

    @Override
    public Response<ServiceInstance> choose(String serviceId, List<ServiceInstance> instances, Optional<GrayContext> optional){
        log.debug("nacos service {} instances: {}",serviceId,instances);
        if (instances.isEmpty()) {
            log.warn("No servers available for service: " + serviceId);
            return new EmptyResponse();
        }
        if(!optional.isPresent()) {
            int pos = Math.abs(position.incrementAndGet());
            ServiceInstance instance = instances.get(pos % instances.size());
            log.debug("gateway chose service instance: {}", instance.getInstanceId());
            return new DefaultResponse(instance);
        }

        GrayContext ctx= optional.get();

        List<ServiceInstance> availableInstances= Collections.emptyList();
        if(!CollectionUtils.isEmpty(ctx.getInVers())){
            availableInstances= instances.stream().filter((ServiceInstance instance)->{
                String version= instance.getMetadata().get("version");
                return ctx.getInVers().contains(version);
            }).collect(Collectors.toList());
        }
        if(CollectionUtils.isEmpty(availableInstances)){
            if(CollectionUtils.isEmpty(ctx.getExVers())) {
                availableInstances = instances.stream().filter((ServiceInstance instance) -> {
                    String version = instance.getMetadata().get("version");
                    return !ctx.getExVers().contains(version);
                }).collect(Collectors.toList());
            }
            else{
                availableInstances= instances;
            }
        }
        if(!CollectionUtils.isEmpty(availableInstances)){
            ServiceInstance instance= availableInstances.get(Math.abs(position.incrementAndGet()) % availableInstances.size());
            return new DefaultResponse(instance);
        }
        log.warn("cannot find appropriate match candidate server: {}",ctx);
        return new EmptyResponse();
    }
}