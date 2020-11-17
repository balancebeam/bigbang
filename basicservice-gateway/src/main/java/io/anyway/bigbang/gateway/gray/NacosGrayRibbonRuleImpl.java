package io.anyway.bigbang.gateway.gray;

import io.anyway.bigbang.framework.gray.GrayContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Response;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Slf4j
public class NacosGrayRibbonRuleImpl implements GrayRibbonRule{

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
        List<ServiceInstance> availableInstances= instances.stream().filter(each-> {
            String cluster= each.getMetadata().get("nacos.cluster");
            return cluster.equals(ctx.getGroup());
        }).collect(Collectors.toList());
        //lookup the default group and cluster
        if(availableInstances.isEmpty() &&
                ctx.getDefGroup()!= null &&
                !ctx.getGroup().equals(ctx.getDefGroup())){
            availableInstances= instances.stream().filter(each-> {
                String cluster= each.getMetadata().get("nacos.cluster");
                return cluster.equals(ctx.getDefGroup());
            }).collect(Collectors.toList());
        }
        if(!availableInstances.isEmpty()){

            ServiceInstance instance= availableInstances.get(Math.abs(position.incrementAndGet()) % availableInstances.size());
            return new DefaultResponse(instance);
        }
        log.warn("cannot find appropriate match candidate server: {}",ctx.toString());
        return new EmptyResponse();
    }
}