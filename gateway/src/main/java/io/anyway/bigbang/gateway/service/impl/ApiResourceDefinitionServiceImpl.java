package io.anyway.bigbang.gateway.service.impl;

import io.anyway.bigbang.gateway.domain.ApiResource;
import io.anyway.bigbang.gateway.service.ApiResourceDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "spring.cloud.gateway.authentication-validator",name="enabled",havingValue = "true")
public class ApiResourceDefinitionServiceImpl implements ApiResourceDefinitionService {

    final private Map<String, ApiResource> apiResourceMap;

    final private Method convertToRoute;

    @Resource
    private RouteDefinitionRouteLocator routeLocator;

    public ApiResourceDefinitionServiceImpl(){
        apiResourceMap= new HashMap<>();
        convertToRoute= ReflectionUtils.findMethod(RouteDefinitionRouteLocator.class,"convertToRoute",RouteDefinition.class);
        ReflectionUtils.makeAccessible(convertToRoute);
    }

    @PostConstruct
    public void init(){
        //TODO ,read api resource from db or remote service
        ApiResource resource= new ApiResource();
        resource.setServiceId("example-provider");
        resource.setCode("CI14003");
        resource.setPath("/api/user/jerry");
        resource.setRoute(createRoute(resource.getServiceId()));
        apiResourceMap.put(resource.getCode(),resource);
        resource= new ApiResource();
        resource.setServiceId("example-provider");
        resource.setCode("CI14004");
        resource.setPath("/api/user");
        resource.setRoute(createRoute(resource.getServiceId()));
        apiResourceMap.put(resource.getCode(),resource);
    }

    private Route createRoute(String serviceId){
        RouteDefinition routeDefinition= new RouteDefinition();
        routeDefinition.setId(serviceId);
        try {
            routeDefinition.setUri(new URI("lb://"+serviceId));
        } catch (URISyntaxException e) {

        }
//        FilterDefinition filterDefinition= new FilterDefinition();
//        filterDefinition.setName("StripPrefix");
//        filterDefinition.addArg("parts","0");
//        routeDefinition.setFilters(Arrays.asList(filterDefinition));
//
//        PredicateDefinition predicateDefinition= new PredicateDefinition();
//        predicateDefinition.setName("Path");
//        predicateDefinition.addArg("pattern","/api/**" );
//        routeDefinition.setPredicates(Arrays.asList(predicateDefinition));
        log.info("Api Resource RouteDefinition: {}",routeDefinition);
        return (Route)ReflectionUtils.invokeMethod(convertToRoute,routeLocator,routeDefinition);
    }

    @Override
    public Optional<ApiResource> getApiResource(String serviceCode) {
        ApiResource apiResource= apiResourceMap.get(serviceCode);
        return apiResource!= null? Optional.of(apiResource): Optional.empty();
    }
}
