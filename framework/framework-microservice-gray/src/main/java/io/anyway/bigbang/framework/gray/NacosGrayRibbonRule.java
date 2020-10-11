package io.anyway.bigbang.framework.gray;

import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.google.common.base.Optional;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class NacosGrayRibbonRule extends ZoneAvoidanceRule {

    private String serviceId;

    @Override
    public void setLoadBalancer(ILoadBalancer lb) {
        super.setLoadBalancer(lb);
        if (lb instanceof BaseLoadBalancer) {
            serviceId = ((BaseLoadBalancer) lb).getName().toLowerCase().trim();
            log.info("Setting service {} loader", serviceId);
        }
    }

    @Override
    public Server choose(Object key) {
        ILoadBalancer lb = getLoadBalancer();
        List<Server> availableServers = getAvailableServers(lb.getAllServers());
        Optional<Server> server = getPredicate().chooseRoundRobinAfterFiltering(availableServers, key);
        if (server.isPresent()) {
            return server.get();
        } else {
            return null;
        }
    }

    private List<Server> getAvailableServers(List<Server> allServers) {
        final java.util.Optional<GrayContext> grayContext= GrayContextHolder.getGrayContext();
        if(grayContext.isPresent()) {
            GrayContext ctx= grayContext.get();
            List<Server> availableServers= allServers.stream().filter((Server server)->{
                String cluster= ((NacosServer)server).getInstance().getClusterName();
                return cluster.equals(ctx.getGroup());
            }).collect(Collectors.toList());

            //lookup the default group and cluster
            if(availableServers.isEmpty() &&
                    ctx.getDefGroup()!= null &&
                    !ctx.getGroup().equals(ctx.getDefGroup())){
                availableServers= allServers.stream().filter(server-> {
                    String cluster= ((NacosServer)server).getInstance().getClusterName();
                    return cluster.equals(ctx.getDefGroup());
                }).collect(Collectors.toList());
            }

            if(CollectionUtils.isEmpty(availableServers)){
                log.warn("cannot find appropriate match candidate server: {}",ctx);
                return Collections.emptyList();
            }
            return availableServers;
        }
        return allServers;
    }
}
