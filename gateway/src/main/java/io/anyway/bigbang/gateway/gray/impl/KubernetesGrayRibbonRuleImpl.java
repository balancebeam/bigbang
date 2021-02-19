package io.anyway.bigbang.gateway.gray.impl;

import io.anyway.bigbang.framework.gray.GrayContext;
import io.anyway.bigbang.gateway.gray.GrayRibbonRule;
import io.fabric8.kubernetes.api.model.EndpointAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.kubernetes.discovery.KubernetesServiceInstance;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class KubernetesGrayRibbonRuleImpl implements GrayRibbonRule {

    private AtomicInteger position= new AtomicInteger(new Random().nextInt(1000));

    final private Field f;

    public KubernetesGrayRibbonRuleImpl(){
        f= ReflectionUtils.findField(KubernetesServiceInstance.class,"endpointAddress");
        ReflectionUtils.makeAccessible(f);
    }

    @Override
    public Response<ServiceInstance> choose(String serviceId, List<ServiceInstance> instances, Optional<GrayContext> optional){
        log.debug("kubernetes service {} instances: {}",serviceId,instances);
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
            availableInstances= instances.stream().filter(each-> {
                EndpointAddress endpointAddress= (EndpointAddress)ReflectionUtils.getField(f,each);
                for(String version: ctx.getInVers()) {
                    if(endpointAddress.getTargetRef().getName().startsWith(serviceId+"-"+version)){
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());
        }
        if(CollectionUtils.isEmpty(availableInstances)){
            if(CollectionUtils.isEmpty(ctx.getExVers())) {
                availableInstances = instances.stream().filter(each-> {
                    EndpointAddress endpointAddress= (EndpointAddress)ReflectionUtils.getField(f,each);
                    for(String version: ctx.getExVers()) {
                        if(endpointAddress.getTargetRef().getName().startsWith(serviceId+"-"+version)){
                            return false;
                        }
                    }
                    return true;
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