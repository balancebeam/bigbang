package io.anyway.bigbang.gateway.service.impl;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.gateway.domain.RouteMetadata;
import io.anyway.bigbang.gateway.service.DynamicRouteService;
import io.anyway.bigbang.gateway.service.ResourceStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

@Slf4j
public class RouteDefinitionStrategyServiceImpl implements ResourceStrategyService {

    @Resource
    private DynamicRouteService dynamicRouteService;

    @Override
    public void addResourceStrategy(String id, String text) {
        if(StringUtils.isEmpty(text)){
            return;
        }
        RouteMetadata routeMetadata= JSONObject.parseObject(text, RouteMetadata.class);
        routeMetadata.setId(id);
        dynamicRouteService.add(routeMetadata);
    }

    @Override
    public void updateResourceStrategy(String id, String text) {
        if(StringUtils.isEmpty(text)){
            return;
        }
        RouteMetadata routeMetadata= JSONObject.parseObject(text, RouteMetadata.class);
        routeMetadata.setId(id);
        dynamicRouteService.update(routeMetadata);
    }

    @Override
    public void removeResourceStrategy(String id) {
        dynamicRouteService.delete(id);
    }
}
