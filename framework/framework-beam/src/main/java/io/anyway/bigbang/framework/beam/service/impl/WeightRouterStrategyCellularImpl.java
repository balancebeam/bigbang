package io.anyway.bigbang.framework.beam.service.impl;

import com.netflix.loadbalancer.Server;
import io.anyway.bigbang.framework.beam.service.RouterStrategyCellular;
import io.anyway.bigbang.framework.beam.service.WeightBalancerService;
import io.anyway.bigbang.framework.beam.service.WeightStrategyQuerier;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class WeightRouterStrategyCellularImpl implements WeightBalancerService, RouterStrategyCellular, WeightStrategyQuerier {
    /**
     * {"192.168.1.20:8000": 100, "192.168.1.21": 20}
     */
    private ConcurrentHashMap<String, Integer> weightRouterStrategyMapping= new ConcurrentHashMap<>();

    private Random random= new Random();

    @Override
    public Server choose(String serviceId,List<Server> servers) {
        if(servers.size() ==1 ){
            return servers.get(0);
        }
        Map<Server, Integer> serverWeightMap = new HashMap<>();
        int totalWeight=0;
        for(Server each: servers){
            int weight= 100;
            Integer val= weightRouterStrategyMapping.get(each.getHostPort());
            if(val!= null && (weight= val)<= 0){
                log.debug("The server {} weight {} <=0, it will be ignored",each.getHost(), weight);
                continue;
            }
			serverWeightMap.put(each, weight);
			totalWeight+= weight;
        }
        log.debug("serviceId {} serverWeightMap: {}",serviceId,serverWeightMap);
        int rdmWeight = random.nextInt(totalWeight);
        log.debug("serviceId {} choose by weight, totalWeight {}, rdmWeight {}",serviceId,totalWeight,rdmWeight);
        int cur = 0;
        for(Map.Entry<Server, Integer> each : serverWeightMap.entrySet()) {
            cur += each.getValue();
            if(rdmWeight< cur) {
                log.info("serviceId {} selected server: {}",serviceId,each.getKey());
                return each.getKey();
            }
        }
        log.info("serviceId {} serverWeightMap was empty, random to select a server");
        return servers.get(random.nextInt(servers.size()));
    }

    @Override
    public void setRouterStrategy(String hostPort,String text) {
        if(StringUtils.isEmpty(text)){
            return;
        }
        log.debug("Setting Weight router strategy, hostPort: {} ,weight: {}",hostPort,text);
        int weight= Integer.parseInt(text);
        weightRouterStrategyMapping.put(hostPort,weight);
        log.info("WeightRouterStrategyMapping: {}",weightRouterStrategyMapping);

    }

    @Override
    public void removeRouterStrategy(String hostPort) {
        log.debug("Removing weight router strategy, hostPort: {}",hostPort);
        weightRouterStrategyMapping.remove(hostPort);
        log.info("WeightRouterStrategyMapping: {}",weightRouterStrategyMapping);
    }

    @Override
    public Collection<Map.Entry<String,Integer>> queryAll() {
        return weightRouterStrategyMapping.entrySet();
    }

    @Override
    public Integer query(String hostPort) {
        return weightRouterStrategyMapping.get(hostPort);
    }
}
