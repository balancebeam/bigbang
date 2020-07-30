package io.anyway.bigbang.framework.kernel.gray;

import com.alibaba.cloud.nacos.ribbon.NacosServer;
import com.google.common.base.Optional;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import io.anyway.bigbang.framework.kernel.header.PrincipleHeaderContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class GrayRibbonRule extends ZoneAvoidanceRule {

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
        final java.util.Optional<String> grayUnit= PrincipleHeaderContext.getHeader(NacosMetadataMapConfig.GRAY_UNIT_NAME);
        if(grayUnit.isPresent()) {
            java.util.Optional<String> opt= PrincipleHeaderContext.getHeader(NacosMetadataMapConfig.GRAY_INDICATOR_NAME);
            String indicator= opt.isPresent()? opt.get(): NacosMetadataMapConfig.GRAY_DEFAULT_INDICATOR;
            List<Server> availableServers= allServers.stream().filter((Server server)->{
                String unit= ((NacosServer)server).getMetadata().get(indicator);
                if(StringUtils.isEmpty(unit)){
                    java.util.Optional<String> defaultOpt= PrincipleHeaderContext.getHeader(NacosMetadataMapConfig.GRAY_INDICATOR_DEFAULT_VALUE_NAME);
                    String defaultValue= defaultOpt.isPresent()? defaultOpt.get(): NacosMetadataMapConfig.GRAY_DEFAULT_UNIT;
                    unit= defaultValue;
                }
                return grayUnit.get().equals(unit);
            }).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(availableServers)){
                return allServers;
            }
            return availableServers;
        }
        return allServers;
    }
}
