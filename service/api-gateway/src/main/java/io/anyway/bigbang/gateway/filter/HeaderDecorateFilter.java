package io.anyway.bigbang.gateway.filter;

import io.anyway.bigbang.framework.core.interceptor.HeaderDeliveryService;
import io.anyway.bigbang.framework.core.security.annotation.InternalApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Component
public class HeaderDecorateFilter implements GlobalFilter, Ordered {

    @Resource
    private HeaderDeliveryService headerDeliveryService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        ServerHttpRequest.Builder builder= req.mutate().path(req.getURI().getRawPath());
        builder.header(InternalApi.HEADER_GATEWAY_KEY,"true");
        for(Map.Entry<String,String> each: headerDeliveryService.headers().entrySet()){
            if(req.getHeaders().getFirst(each.getKey())== null){
                builder.header(each.getKey(),each.getValue());
            }
        }
        ServerHttpRequest request = builder.build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
