package io.anyway.bigbang.framework.gray;

import com.google.common.base.Optional;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Random;

@Slf4j
public class K8SGrayRibbonRule extends ZoneAvoidanceRule {

    private String serviceId;

    @Autowired
    private DiscoveryClient discoveryClient;

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
        List<ServiceInstance> instances= discoveryClient.getInstances(getK8sServiceName(ctx.getGroup()));
        if(CollectionUtils.isEmpty(instances)){
            instances= discoveryClient.getInstances(getK8sServiceName(ctx.getDefGroup()));
        }
        if(!CollectionUtils.isEmpty(instances)){
            int index= random.nextInt(instances.size());
            ServiceInstance instance= instances.get(index);
            return new Server(instance.getHost(),instance.getPort());
        }
        else{
            return null;
        }
    }


    private String getK8sServiceName(String group){
        if(StringUtils.isEmpty(group)){
            return serviceId;
        }
        return serviceId+ "-"+group;
    }
}
