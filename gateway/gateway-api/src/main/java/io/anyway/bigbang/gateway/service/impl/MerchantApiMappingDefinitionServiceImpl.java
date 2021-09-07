package io.anyway.bigbang.gateway.service.impl;

import io.anyway.bigbang.framework.utils.JsonUtil;
import io.anyway.bigbang.gateway.domain.ApiMappingDefinition;
import io.anyway.bigbang.gateway.service.DynamicRouteService;
import io.anyway.bigbang.gateway.service.MerchantApiMappingDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

@Slf4j

public class MerchantApiMappingDefinitionServiceImpl extends AbstractMerchantApiRepository<String> implements MerchantApiMappingDefinitionService {

    private volatile Map<String, ApiMappingDefinition> apiMappingDefinitionHash;

    final private Method convertToRoute;

    @Resource
    private DynamicRouteService dynamicRouteService;

    @Resource
    private RouteDefinitionRouteLocator routeLocator;

    public MerchantApiMappingDefinitionServiceImpl(){
        convertToRoute= ReflectionUtils.findMethod(RouteDefinitionRouteLocator.class,"convertToRoute",RouteDefinition.class);
        ReflectionUtils.makeAccessible(convertToRoute);
    }

    @PostConstruct
    public void init() throws Exception{
        RouteDefinition routeDefinition= new RouteDefinition();
        routeDefinition.setId("merchant-openapi");
        routeDefinition.setOrder(-1);
        routeDefinition.setUri(new URI("lb://merchant-openapi-route"));
        FilterDefinition filterDefinition= new FilterDefinition();
        filterDefinition.setName("StripPrefix");
        Map<String,String> args= new LinkedHashMap<>();
        args.put("parts","1");
        filterDefinition.setArgs(args);
        routeDefinition.setFilters(Arrays.asList(filterDefinition));
        PredicateDefinition predicateDefinition= new PredicateDefinition();
        predicateDefinition.setName("Path");
        args= new LinkedHashMap<>();
        args.put("pattern","/merchant/api");
        predicateDefinition.setArgs(args);
        routeDefinition.setPredicates(Arrays.asList(predicateDefinition));
        log.info("Merchant RouteDefinition: {}",routeDefinition);
        dynamicRouteService.add(routeDefinition);
    }

    @Override
    public Optional<ApiMappingDefinition> getApiMappingDefinition(String apiCode) {
        retrieveApiMapping();
        ApiMappingDefinition apiMappingDefinition= apiMappingDefinitionHash.get(apiCode);
        return apiMappingDefinition!= null? Optional.of(apiMappingDefinition): Optional.empty();
    }

    public void retrieveApiMapping(){
        if(apiMappingDefinitionHash==null) {
            synchronized (this) {
                if (apiMappingDefinitionHash == null){
                    apiMappingDefinitionHash = new LinkedHashMap<>();
                    String apiDefinitionString = execute("/internal/merchant/api-mapping",String.class);
                    List<Map> list = JsonUtil.fromString2Object(apiDefinitionString, List.class);
                    apiMappingDefinitionHash = new LinkedHashMap<>();
                    Map<String, Route> routeMap = new LinkedHashMap<>();
                    for (Map each : list) {
                        ApiMappingDefinition apiMappingDefinition = JsonUtil.fromObject2Object(each);
                        String serviceId = apiMappingDefinition.getServiceId();
                        if (!routeMap.containsKey(serviceId)) {
                            routeMap.put(serviceId, createRoute(serviceId));
                        }
                        apiMappingDefinition.setRoute(routeMap.get(serviceId));
                        apiMappingDefinitionHash.put(apiMappingDefinition.getApiCode(), apiMappingDefinition);
                    }
                }
            }
        }
    }

    private Route createRoute(String serviceId){
        RouteDefinition routeDefinition= new RouteDefinition();
        routeDefinition.setId(serviceId);
        try {
            routeDefinition.setUri(new URI("lb://"+serviceId));
        } catch (URISyntaxException e) {
            log.error("create {} route error",serviceId,e);
        }
        log.info("ApiMapping RouteDefinition: {}",routeDefinition);
        return (Route)ReflectionUtils.invokeMethod(convertToRoute,routeLocator,routeDefinition);
    }


}
