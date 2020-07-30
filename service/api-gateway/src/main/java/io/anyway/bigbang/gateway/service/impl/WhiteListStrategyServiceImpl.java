package io.anyway.bigbang.gateway.service.impl;

import io.anyway.bigbang.gateway.property.GatewayProperties;
import io.anyway.bigbang.gateway.service.ResourceStrategyService;
import io.anyway.bigbang.gateway.service.WhiteListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class WhiteListStrategyServiceImpl implements ResourceStrategyService, WhiteListService {

    @Resource
    private GatewayProperties gatewayProperties;

    final private CopyOnWriteArrayList<ServerWebExchangeMatcher> matchers;

    private OrServerWebExchangeMatcher webExchangeMatcher;

    final private ConcurrentHashMap<String,ServerWebExchangeMatcher> mapping;

    public WhiteListStrategyServiceImpl(){
        matchers= new CopyOnWriteArrayList<>();
        mapping= new ConcurrentHashMap<>();
    }

    @Override
    public void addResourceStrategy(String id, String pattern) {
        ServerWebExchangeMatcher matcher= new PathPatternParserServerWebExchangeMatcher(pattern, null);
        matchers.add(matcher);
        mapping.put(id,matcher);
        log.info("Add PathPatternParserServerWebExchangeMatcher: {}",matcher);
    }

    @Override
    public void updateResourceStrategy(String id, String pattern) {
        ServerWebExchangeMatcher matcher= new PathPatternParserServerWebExchangeMatcher(pattern, null);
        matchers.remove(mapping.get(id));
        matchers.add(matcher);
        mapping.put(id,matcher);
        log.info("Update PathPatternParserServerWebExchangeMatcher: {}",matcher);
    }

    @Override
    public void removeResourceStrategy(String id) {
        if(mapping.containsKey(id)) {
            ServerWebExchangeMatcher matcher = mapping.remove(id);
            matchers.remove(matcher);
            log.info("Remove PathPatternParserServerWebExchangeMatcher: {}", matcher);
        }
    }

    @Override
    public boolean isWhitelistApi(ServerWebExchange exchange) {
        Mono<ServerWebExchangeMatcher.MatchResult> mono= webExchangeMatcher.matches(exchange);
        Mono<Boolean> result= mono.map(r -> r.isMatch());
        try {
            return result.toFuture().get();
        } catch (Exception e) {
           log.error("Execute WebExchangeMatch error",e);
           return false;
        }
    }

    @PostConstruct
    public void init(){
        gatewayProperties.getPathWhiteList().forEach(path->matchers.add(new PathPatternParserServerWebExchangeMatcher(path,null)));
        gatewayProperties.setPathWhiteList(null);
        webExchangeMatcher= new OrServerWebExchangeMatcher(matchers);
    }
}
