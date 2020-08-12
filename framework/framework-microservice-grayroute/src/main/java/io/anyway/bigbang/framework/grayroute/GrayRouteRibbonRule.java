package io.anyway.bigbang.framework.grayroute;

import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.google.common.base.Optional;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import io.anyway.bigbang.framework.discovery.GrayRouteContext;
import io.anyway.bigbang.framework.discovery.GrayRouteContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

import static io.anyway.bigbang.framework.discovery.GrayRouteContext.ATTRIBUTE_CLUSTER_NAME;
import static io.anyway.bigbang.framework.discovery.GrayRouteContext.ATTRIBUTE_GROUP;

@Slf4j
public class GrayRouteRibbonRule extends ZoneAvoidanceRule {

    private String serviceId;

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
        List<Server> availableServers= getAvailableServers(lb.getAllServers());
        Optional<Server> server = getPredicate().chooseRoundRobinAfterFiltering(availableServers, key);
        if (server.isPresent()) {
            return server.get();
        } else {
            return null;
        }
    }

    private List<Server> getAvailableServers(List<Server> allServers){
        final java.util.Optional<GrayRouteContext> grayRouteContext= GrayRouteContextHolder.getGrayRouteContext();
        if(grayRouteContext.isPresent()) {
            GrayRouteContext ctx= grayRouteContext.get();
            List<Server> availableServers= allServers.stream().filter((Server server)->{
                String cluster= ((NacosServer)server).getMetadata().get(ATTRIBUTE_CLUSTER_NAME);
                return cluster.equals(ctx.getCluster());
            }).collect(Collectors.toList());

            //lookup the default group and cluster
            if(availableServers.isEmpty() &&
                    ctx.getDefaultCluster()!= null &&
                    !ctx.getCluster().equals(ctx.getDefaultCluster())){
                availableServers= allServers.stream().filter(server-> {
                    String cluster= ((NacosServer)server).getMetadata().get(ATTRIBUTE_CLUSTER_NAME);
                    return cluster.equals(ctx.getDefaultCluster());
                }).collect(Collectors.toList());
            }

            if(CollectionUtils.isEmpty(availableServers)){
                log.warn("cannot find appropriate match candidate server: {}",ctx);
                return Collections.EMPTY_LIST;
            }
            return availableServers;
        }
        return allServers;
    }
}
