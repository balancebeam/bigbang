package io.anyway.bigbang.gateway.service;

import io.anyway.bigbang.gateway.domain.RouteMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.*;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class DynamicRouteService implements ApplicationEventPublisherAware {

    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    private ApplicationEventPublisher publisher;

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Autowired
    private RouteLocator routeLocator;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher= applicationEventPublisher;
    }

    public RouteDefinition add(RouteMetadata routeMetadata){
        RouteDefinition definition= transform(routeMetadata);
        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        publisher.publishEvent(new RefreshRoutesEvent(this));
        return definition;
    }

    public RouteDefinition update(RouteMetadata routeMetadata) {
        RouteDefinition definition= transform(routeMetadata);
        if(getRouteById(definition.getId()).block().hasBody()) {
            try {
                this.routeDefinitionWriter.delete(Mono.just(definition.getId()));
            } catch (Exception e) {
                log.warn("update error in delete old", e);
            }
        }
        routeDefinitionWriter.save(Mono.just(definition)).subscribe();
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
        return definition;
    }

    public Mono<ResponseEntity<Object>> delete(String id) {
        return this.routeDefinitionWriter.delete(Mono.just(id))
                .then(Mono.defer(() -> Mono.just(ResponseEntity.ok().build())))
                .onErrorResume(t -> t instanceof NotFoundException, t -> Mono.just(ResponseEntity.notFound().build()));
    }

    public Mono<List<Map<String, Object>>> getRoutesList() {
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
        });
    }

    public Mono<ResponseEntity<RouteDefinition>> getRouteById(String id) {
        return this.routeDefinitionLocator.getRouteDefinitions()
                .filter(route -> route.getId().equals(id))
                .singleOrEmpty()
                .map(route -> ResponseEntity.ok(route))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    private RouteDefinition transform(RouteMetadata metadata){
        log.info("RouteMetadata: {}",metadata);
        RouteDefinition routeDefinition= new RouteDefinition();
        routeDefinition.setId(metadata.getId());
        PredicateDefinition predicateDefinition= new PredicateDefinition();
        predicateDefinition.setName("Path");
        Map<String, String> predicateParams = new HashMap<>(8);
        predicateParams.put("pattern", metadata.getPath());
        predicateDefinition.setArgs(predicateParams);
        List<PredicateDefinition> predicateDefinitions= new ArrayList<>();
        predicateDefinitions.add(predicateDefinition);
        routeDefinition.setPredicates(predicateDefinitions);
        if(metadata.isRewritePath()) {
            FilterDefinition filterDefinition = new FilterDefinition();
            filterDefinition.setName("RewritePath");
            filterDefinition.addArg("regexp", metadata.getRegexp());
            filterDefinition.addArg("replacement", metadata.getReplacement());
            List<FilterDefinition> filterDefinitions = new ArrayList<>();
            filterDefinitions.add(filterDefinition);
            routeDefinition.setFilters(filterDefinitions);
        }

        try {
            routeDefinition.setUri(new URI(metadata.getUri()));
        } catch (URISyntaxException e) {
            log.error(e.getMessage());
        }
        log.info("RouteDefinition: {}",routeDefinition);
        return routeDefinition;
    }

    @PostConstruct
    public void init(){
        RouteMetadata routeMetadata= new RouteMetadata();
        routeMetadata.setId("1");
        routeMetadata.setUri("lb://example");
        routeMetadata.setPath("/api/foo/**");
        routeMetadata.setRewritePath(true);
        routeMetadata.setRegexp("/api/foo");
        routeMetadata.setReplacement("");
        add(routeMetadata);
    }

}
