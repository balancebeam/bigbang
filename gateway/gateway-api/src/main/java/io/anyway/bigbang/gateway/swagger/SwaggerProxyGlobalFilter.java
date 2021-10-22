package io.anyway.bigbang.gateway.swagger;

import io.anyway.bigbang.gateway.service.DynamicRouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRouteLocator;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@Slf4j
public class SwaggerProxyGlobalFilter implements GlobalFilter, Ordered {

    final private Method convertToRoute;

    @Resource
    private DynamicRouteService dynamicRouteService;

    @Resource
    private RouteDefinitionRouteLocator routeLocator;

    public SwaggerProxyGlobalFilter(){
        convertToRoute= ReflectionUtils.findMethod(RouteDefinitionRouteLocator.class,"convertToRoute", RouteDefinition.class);
        ReflectionUtils.makeAccessible(convertToRoute);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        String path= req.getPath().toString();
        if(path.startsWith("/swagger-proxy")){
            String serviceId= path.split("/")[2];
            exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, createRoute(serviceId));
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE+1000090;
    }

    @PostConstruct
    public void init() throws Exception{
        RouteDefinition routeDefinition= new RouteDefinition();
        routeDefinition.setId("swagger-proxy");
        routeDefinition.setOrder(-1);
        routeDefinition.setUri(new URI("lb://swagger-proxy"));
        FilterDefinition filterDefinition= new FilterDefinition();
        filterDefinition.setName("StripPrefix");
        Map<String,String> args= new LinkedHashMap<>();
        args.put("parts","2");
        filterDefinition.setArgs(args);
        routeDefinition.setFilters(Arrays.asList(filterDefinition));
        PredicateDefinition predicateDefinition= new PredicateDefinition();
        predicateDefinition.setName("Path");
        args= new LinkedHashMap<>();
        args.put("pattern","/swagger-proxy/**");
        predicateDefinition.setArgs(args);
        routeDefinition.setPredicates(Arrays.asList(predicateDefinition));
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("NO_SWAGGER",true);
        routeDefinition.setMetadata(metadata);
        log.info("Swagger proxy RouteDefinition: {}",routeDefinition);
        dynamicRouteService.add(routeDefinition);
    }

    private Route createRoute(String serviceId){
        RouteDefinition routeDefinition= new RouteDefinition();
        routeDefinition.setId(serviceId);
        try {
            routeDefinition.setUri(new URI("lb://"+serviceId));
        } catch (URISyntaxException e) {
            log.error("create {} route error",serviceId,e);
        }
        log.info("Swagger Proxy RouteDefinition: {}",routeDefinition);
        return (Route)ReflectionUtils.invokeMethod(convertToRoute,routeLocator,routeDefinition);
    }
}
