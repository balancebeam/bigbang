package io.anyway.bigbang.gateway.config;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import io.anyway.bigbang.gateway.service.DynamicRouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RefreshScope
@Configuration
public class DynamicRouteConfig {

    private Map<String, RouteDefinition> ROUTE_MAPPING= new HashMap<>();

    @Value("${spring.gateway.dynamic-route.dataId:gateway-dynamic-route}")
    private String dataId;

    @Value("${spring.gateway.dynamic-route.group:DEFAULT_GROUP}")
    private String group;

    @Value("${spring.cloud.nacos.config.server-addr}")
    private String serverAddr;

    @Resource
    private DynamicRouteService dynamicRouteService;

    @PostConstruct
    public void dynamicRouteByNacosListener() {
        try {
            ConfigService configService = NacosFactory.createConfigService(serverAddr);

            // When the app startups, fetch gateway dynamic router information firstly.
            String configInfo = configService.getConfig(dataId, group, 5000);
            addAndPublishBatchRoute(configInfo);

            // Add gateway router listener
            configService.addListener(dataId, group, new AbstractListener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    addAndPublishBatchRoute(configInfo);
                }
            });
        } catch (NacosException e) {
            log.error("init route definition error",e);
        }
    }

    private synchronized void addAndPublishBatchRoute(String text){
        if(StringUtils.isEmpty(text)){
            return;
        }
        List<RouteDefinition> gatewayRouteDefinitions = JSONObject.parseArray(text,RouteDefinition.class);
        RouteDefinition routeDefinition;
        Map<String,RouteDefinition> newMapping= new HashMap<>();
        for(RouteDefinition each: gatewayRouteDefinitions){
            routeDefinition= ROUTE_MAPPING.get(each.getId());
            if(routeDefinition!= null){
                if(!routeDefinition.equals(each)){
                    dynamicRouteService.update(each);
                }
                ROUTE_MAPPING.remove(each.getId());
            }
            else{
                dynamicRouteService.add(each);
            }
            newMapping.put(each.getId(),each);
        }
        for(String id: ROUTE_MAPPING.keySet()){
            dynamicRouteService.delete(id);
        }
        ROUTE_MAPPING= newMapping;
        log.info("ROUTE_MAPPING: {}",ROUTE_MAPPING);
    }

}
