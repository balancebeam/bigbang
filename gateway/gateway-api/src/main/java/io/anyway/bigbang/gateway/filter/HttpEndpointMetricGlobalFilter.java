package io.anyway.bigbang.gateway.filter;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
public class HttpEndpointMetricGlobalFilter implements GlobalFilter, Ordered, MeterBinder {

    private MeterRegistry registry;

    private Counter endpoint_qps;

    private Counter endpoint_tps;

    final private Set<String> QUERY_METHODS= new HashSet<>(Arrays.asList("GET", "TRACE"));

    final private Set<String> TRANSACTION_METHODS= new HashSet<>(Arrays.asList("POST", "UPDATE", "DELETE"));

    private volatile Map<Integer, Counter> counterMap= new HashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        beginEndpoint(exchange.getRequest());
        return chain.filter(exchange)
                .doOnSuccess(aVoid -> endRespectingCommit(exchange))
                .doOnError(throwable -> endRespectingCommit(exchange));
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER + 1;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.registry= registry;
        this.endpoint_qps = Counter.builder("http.endpoint.request.qps").register(registry);
        this.endpoint_tps = Counter.builder("http.endpoint.request.tps").register(registry);
    }

    private void beginEndpoint(ServerHttpRequest request) {
        String method = request.getMethod().name().toUpperCase();
        if (QUERY_METHODS.contains(method)) {
            endpoint_qps.increment();
        } else if (TRANSACTION_METHODS.contains(method)) {
            String statistic2Query= request.getQueryParams().getFirst("STATISTIC2QUERY");
            if("true".equals(statistic2Query)){
                endpoint_qps.increment();
            }
            else {
                endpoint_tps.increment();
            }
        } else {
            endpoint_qps.increment();
            log.debug("Not support statistic method: {}", method);
        }
    }

    private void endRespectingCommit(ServerWebExchange exchange){
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            endTimerInner(exchange);
        }
        else {
            response.beforeCommit(() -> {
                endTimerInner(exchange);
                return Mono.empty();
            });
        }
    }

    private void endTimerInner(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        int rawStatusCode= response.getRawStatusCode();
        Counter counter= counterMap.get(rawStatusCode);
        if(counter== null){
            synchronized (counterMap) {
                counter= counterMap.get(rawStatusCode);
                if(counter== null) {
                    counter = Counter.builder("http.endpoint.response.status." + rawStatusCode).register(registry);
                    counterMap.put(rawStatusCode,counter);
                    log.info("add new metric response status: {}",rawStatusCode);
                }
            }
        }
        counter.increment();
    }

}
