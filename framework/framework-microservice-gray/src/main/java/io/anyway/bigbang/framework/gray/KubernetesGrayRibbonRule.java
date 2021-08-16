package io.anyway.bigbang.framework.gray;

import com.google.common.base.Optional;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import io.fabric8.kubernetes.api.model.EndpointAddress;
import io.fabric8.kubernetes.api.model.Endpoints;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.kubernetes.discovery.KubernetesDiscoveryClient;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
public class KubernetesGrayRibbonRule extends ZoneAvoidanceRule {

    private AtomicInteger pos= new AtomicInteger(new Random().nextInt(1000));

    private String serviceId;

    @Resource
    private KubernetesDiscoveryClient kubernetesDiscoveryClient;

    private Random random= new Random();

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
        List<ServiceInstance> instances= kubernetesDiscoveryClient.getInstances(serviceId);
        log.debug("kubernetes service {} instances: {}",serviceId,instances);
        if(CollectionUtils.isEmpty(instances)){
            return null;
        }
        Map<String,EndpointAddress> endpointAddressMap= new LinkedHashMap<>();
        List<Endpoints> endpointsList= kubernetesDiscoveryClient.getEndPointsList(serviceId);
        for(Endpoints each: endpointsList){
            EndpointAddress endpointAddress= each.getSubsets().get(0).getAddresses().get(0);
            endpointAddressMap.put(endpointAddress.getTargetRef().getUid(),endpointAddress);
        }

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
            ServiceInstance instance= availableInstances.get(Math.abs(pos.incrementAndGet()) % availableInstances.size());
            return new Server(instance.getHost(),instance.getPort());

        }
        log.warn("cannot find appropriate match candidate server: {}",ctx);
        return null;
    }
}
