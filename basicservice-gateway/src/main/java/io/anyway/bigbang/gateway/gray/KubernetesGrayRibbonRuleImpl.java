package io.anyway.bigbang.gateway.gray;

import io.anyway.bigbang.framework.gray.GrayContext;
import io.fabric8.kubernetes.api.model.EndpointAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.kubernetes.discovery.KubernetesServiceInstance;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class KubernetesGrayRibbonRuleImpl implements GrayRibbonRule{

    private AtomicInteger pos= new AtomicInteger(new Random().nextInt(1000));

    final private Field f;

    public KubernetesGrayRibbonRuleImpl(){
        f= ReflectionUtils.findField(KubernetesServiceInstance.class,"endpointAddress");
        ReflectionUtils.makeAccessible(f);
    }

    @Override
    public Response<ServiceInstance> choose(String serviceId, List<ServiceInstance> instances, GrayContext ctx){
        log.debug("kubernetes service {} instances: {}",serviceId,instances);
        final String serviceName= serviceId+"-"+ctx.getGroup();
        List<ServiceInstance> availableInstances= instances.stream().filter(each-> {
            EndpointAddress endpointAddress= (EndpointAddress)ReflectionUtils.getField(f,each);
            return endpointAddress.getTargetRef().getName().startsWith(serviceName);
        }).collect(Collectors.toList());
        //lookup the default group and cluster
        if(availableInstances.isEmpty() &&
                ctx.getDefGroup()!= null &&
                !ctx.getGroup().equals(ctx.getDefGroup())){
            final String serviceName2= serviceId+"-"+ctx.getDefGroup();
            availableInstances= instances.stream().filter(each-> {
                EndpointAddress endpointAddress= (EndpointAddress)ReflectionUtils.getField(f,each);
                return endpointAddress.getTargetRef().getName().startsWith(serviceName2);
            }).collect(Collectors.toList());
        }
        if(!availableInstances.isEmpty()){
            ServiceInstance instance= availableInstances.get(Math.abs(pos.incrementAndGet()) % availableInstances.size());
            return new DefaultResponse(instance);
        }
        log.warn("cannot find appropriate match candidate server: {}",ctx.toString());
        return new EmptyResponse();
    }
}