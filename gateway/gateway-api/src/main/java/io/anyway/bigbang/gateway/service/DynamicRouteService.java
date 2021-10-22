package io.anyway.bigbang.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.route.*;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
public class DynamicRouteService implements ApplicationEventPublisherAware {

    @Resource
    private RouteDefinitionWriter routeDefinitionWriter;

    private ApplicationEventPublisher publisher;

    @Resource
    private RouteDefinitionLocator routeDefinitionLocator;

    @Resource
    private RouteLocator routeLocator;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher= applicationEventPublisher;
    }

    public void add(RouteDefinition definition){
        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        publisher.publishEvent(new RefreshRoutesEvent(definition));
    }

    public void update(RouteDefinition definition) {
        if(getRouteById(definition.getId()).block().hasBody()) {
            try {
                this.routeDefinitionWriter.delete(Mono.just(definition.getId()));
            } catch (Exception e) {
//                log.warn("update error in delete old", e);
            }
        }
        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        this.publisher.publishEvent(new RefreshRoutesEvent(definition));
    }

    public void delete(String id) {
        this.routeDefinitionWriter.delete(Mono.just(id))
            .then(Mono.defer(() -> Mono.just(ResponseEntity.ok().build())))
            .onErrorResume(t -> t instanceof NotFoundException, t -> Mono.just(ResponseEntity.notFound().build()));
    }

    public List<RouteDefinition> getRouteRouteDefinitionList() {
        try {
            return routeDefinitionLocator.getRouteDefinitions().collectList().toFuture().get();
        }catch (Exception e){
            log.error("get route definition list error",e);
            return Collections.emptyList();
        }
    }

    public List<Map<String, Object>> getRoutesList() throws Exception{
        Mono<Map<String, RouteDefinition>> routeDefs = routeDefinitionLocator.getRouteDefinitions()
                .collectMap(RouteDefinition::getId);
        Mono<List<Route>> routes = this.routeLocator.getRoutes().collectList();

        return Mono.zip(routeDefs, routes).map(tuple -> {
            Map<String, RouteDefinition> defs = tuple.getT1();
            List<Route> routeList = tuple.getT2();
            List<Map<String, Object>> allRoutes = new ArrayList<>();

            routeList.forEach(route -> {
                HashMap<String, Object> r = new HashMap<>();
                r.put("route_id", route.getId());
                r.put("order", route.getOrder());
                if (defs.containsKey(route.getId())) {
                    r.put("route_definition", defs.get(route.getId()));
                } else {
                    HashMap<String, Object> obj = new HashMap<>();
                    obj.put("predicate", route.getPredicate().toString());
                    if (!route.getFilters().isEmpty()) {
                        ArrayList<String> filters = new ArrayList<>();
                        for (GatewayFilter filter : route.getFilters()) {
                            filters.add(filter.toString());
                        }
                        obj.put("filters", filters);
                    }
                    if (!obj.isEmpty()) {
                        r.put("route_object", obj);
                    }
                }
                allRoutes.add(r);
            });

            return allRoutes;
        }).toFuture().get();
    }

    public Mono<ResponseEntity<RouteDefinition>> getRouteById(String id) {
        return this.routeDefinitionLocator.getRouteDefinitions()
                .filter(route -> route.getId().equals(id))
                .singleOrEmpty()
                .map(route -> ResponseEntity.ok(route))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

}
