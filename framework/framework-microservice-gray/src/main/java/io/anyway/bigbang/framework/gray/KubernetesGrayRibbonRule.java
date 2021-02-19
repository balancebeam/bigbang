package io.anyway.bigbang.framework.gray;

import com.google.common.base.Optional;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import io.fabric8.kubernetes.api.model.EndpointAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.kubernetes.discovery.KubernetesServiceInstance;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class KubernetesGrayRibbonRule extends ZoneAvoidanceRule {

    private AtomicInteger pos= new AtomicInteger(new Random().nextInt(1000));

    private String serviceId;

    @Autowired
    private DiscoveryClient discoveryClient;

    private Random random= new Random();

    private static Field f;

    static {
        f= ReflectionUtils.findField(KubernetesServiceInstance.class,"endpointAddress");
        ReflectionUtils.makeAccessible(f);
    }

    @Override
    public void setLoadBalancer(ILoadBalancer lb) {
        super.setLoadBalancer(lb);
        if(lb instanceof BaseLoadBalancer){
            serviceId = ((BaseLoadBalancer)lb).getName().toLowerCase().trim();
            log.info("Setting service {} loader", serviceId);
        }
    }

    @Override
    public Server choose(Object key) {
        ILoadBalancer lb = getLoadBalancer();
        final java.util.Optional<GrayContext> grayContext= GrayContextHolder.getGrayContext();
        if(!grayContext.isPresent()) {
            Optional<Server> server = getPredicate().chooseRoundRobinAfterFiltering(lb.getAllServers(), key);
            if (server.isPresent()) {
                return server.get();
            } else {
                return null;
            }
        }

        GrayContext ctx=grayContext.get();
        List<ServiceInstance> instances= discoveryClient.getInstances(serviceId);
        log.debug("kubernetes service {} instances: {}",serviceId,instances);
        if(CollectionUtils.isEmpty(instances)){
            return null;
        }
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
            ServiceInstance instance= availableInstances.get(Math.abs(pos.incrementAndGet()) % availableInstances.size());
            return new Server(instance.getHost(),instance.getPort());

        }
        log.warn("cannot find appropriate match candidate server: {}",ctx);
        return null;
    }
}
