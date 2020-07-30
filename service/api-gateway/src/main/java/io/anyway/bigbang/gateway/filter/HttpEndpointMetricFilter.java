package io.anyway.bigbang.gateway.filter;

import io.anyway.bigbang.framework.metrics.HttpEndpointMetricCollector;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

@Slf4j
@Component
@ConditionalOnClass(MeterBinder.class)
public class HttpEndpointMetricFilter implements GlobalFilter, Ordered {

    @Resource
    private HttpEndpointMetricCollector httpEndpointMetricCollector;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String method= exchange.getRequest().getMethod().name();
        httpEndpointMetricCollector.count(method);
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1;
    }


}
