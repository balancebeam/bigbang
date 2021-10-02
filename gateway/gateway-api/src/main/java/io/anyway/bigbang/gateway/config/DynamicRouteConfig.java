package io.anyway.bigbang.gateway.config;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import io.anyway.bigbang.gateway.service.DynamicRouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


@Slf4j
@Configuration
public class DynamicRouteConfig implements SmartInitializingSingleton {

    private Map<String, RouteDefinition> ROUTE_MAPPING= new HashMap<>();

    @Value("${spring.cloud.gateway.dynamic-route.dataId:gateway-dynamic-route}")
    private String dataId;

    @Resource
    private NacosConfigProperties nacosConfigProperties;

    @Resource
    private DynamicRouteService dynamicRouteService;

    @Override
    public void afterSingletonsInstantiated() {
        try {
            Properties properties = new Properties();
            if(!StringUtils.isEmpty(nacosConfigProperties.getNamespace())){
                properties.put(PropertyKeyConst.NAMESPACE, nacosConfigProperties.getNamespace());
            }
            properties.put(PropertyKeyConst.SERVER_ADDR, nacosConfigProperties.getServerAddr());
            properties.put("fileExtension","json");
            if(!StringUtils.isEmpty(nacosConfigProperties.getUsername())){
                properties.put(PropertyKeyConst.USERNAME,nacosConfigProperties.getUsername());
                properties.put(PropertyKeyConst.PASSWORD,nacosConfigProperties.getPassword());
            }

            ConfigService configService = NacosFactory.createConfigService(properties);
            // When the app startups, fetch gateway gray router information firstly.
            String configInfo =configService.getConfigAndSignListener(dataId, nacosConfigProperties.getGroup(), 5000, new AbstractListener() {
                @Override
                public void receiveConfigInfo(String configInfo) {
                    addAndPublishBatchRoute(configInfo);
                }
            });
            addAndPublishBatchRoute(configInfo);
        } catch (NacosException e) {
            log.error("init route definition error",e);
        }
    }

    private synchronized void addAndPublishBatchRoute(String text){
        if(StringUtils.isEmpty(text)){
            text= "[]";
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

