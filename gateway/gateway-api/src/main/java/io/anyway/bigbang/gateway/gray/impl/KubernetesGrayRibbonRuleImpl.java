package io.anyway.bigbang.gateway.gray.impl;

import io.anyway.bigbang.framework.gray.GrayContext;
import io.anyway.bigbang.framework.utils.JsonUtil;
import io.anyway.bigbang.gateway.gray.GrayRibbonRule;
import io.fabric8.kubernetes.api.model.EndpointAddress;
import io.fabric8.kubernetes.api.model.Endpoints;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryClient;
import org.springframework.cloud.kubernetes.discovery.KubernetesServiceInstance;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class KubernetesGrayRibbonRuleImpl implements GrayRibbonRule {

    private AtomicInteger position= new AtomicInteger(new Random().nextInt(1000));

    @Resource
    private KubernetesDiscoveryClient kubernetesDiscoveryClient;

    @Override
    public Response<ServiceInstance> choose(String serviceId,
                                            List<ServiceInstance> instances,
                                            Optional<GrayContext> optional){
        if(log.isDebugEnabled()) {
            log.debug("kubernetes service {} instances: {}", serviceId, JsonUtil.fromObject2String(instances));
        }
        if (instances.isEmpty()) {
            log.warn("No servers available for service: " + serviceId);
            return new EmptyResponse();
        }
        if(!optional.isPresent()) {
            int pos = Math.abs(position.incrementAndGet());
            ServiceInstance instance = instances.get(pos % instances.size());
            if(log.isDebugEnabled()) {
                log.debug("gateway chose service {} instance: {}", serviceId,JsonUtil.fromObject2String(instance));
            }
            return new DefaultResponse(reviseIncorrectPort(instance));
        }
        Map<String,EndpointAddress> endpointAddressMap= new LinkedHashMap<>();
        List<Endpoints> endpointsList= kubernetesDiscoveryClient.getEndPointsList(serviceId);
        for(Endpoints each: endpointsList){
            EndpointAddress endpointAddress= each.getSubsets().get(0).getAddresses().get(0);
            endpointAddressMap.put(endpointAddress.getTargetRef().getUid(),endpointAddress);
        }
        GrayContext ctx= optional.get();

        List<ServiceInstance> availableInstances= Collections.emptyList();
        if(!CollectionUtils.isEmpty(ctx.getInVers())){
            availableInstances= instances.stream().filter(each-> {
                EndpointAddress endpointAddress= endpointAddressMap.get(each.getInstanceId());
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
                    EndpointAddress endpointAddress= endpointAddressMap.get(each.getInstanceId());
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
            return new DefaultResponse(reviseIncorrectPort(instance));

        }
        log.warn("cannot find appropriate match candidate server: {}",ctx);
        return new EmptyResponse();
    }

    private ServiceInstance reviseIncorrectPort(ServiceInstance instance){
        if(instance instanceof KubernetesServiceInstance){
            Map<String, String> map= instance.getMetadata();
            if(!CollectionUtils.isEmpty(map)
                    && map.containsKey("port.http")
                    && Integer.parseInt(map.get("port.http"))!=instance.getPort() ){
                log.info("KubernetesServiceInstance({}) had incorrect port, amending port from {} to {}",instance.getUri(),instance.getPort(),map.get("port.http"));
                return new KubernetesServiceInstance(
                        instance.getInstanceId(),
                        instance.getServiceId(),
                        instance.getHost(),
                        Integer.parseInt(map.get("port.http")),
                        instance.getMetadata(),
                        instance.isSecure());
            }
        }
        return instance;
    }


}