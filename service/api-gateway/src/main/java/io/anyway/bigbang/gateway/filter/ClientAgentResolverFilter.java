package io.anyway.bigbang.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.core.client.ClientAgent;
import io.anyway.bigbang.framework.core.client.ClientAgentContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ClientAgentResolverFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest httpRequest= exchange.getRequest();
        String remoteAddr =httpRequest.getRemoteAddress().getHostName();
        ClientAgentContextHolder.remove();
        if(log.isInfoEnabled()) {
            log.info("request path:{}",httpRequest.getPath());
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            for (String headerName: httpRequest.getHeaders().keySet()) {
                String headerValue = httpRequest.getHeaders().getFirst(headerName);
                builder.append(headerName);
                builder.append("=");
                builder.append(headerValue);
                builder.append(",");
            }
            builder.append("}");
            log.info("HttpServletRequest headers:{}, remote address: {}", builder.toString(),remoteAddr);
        }
        String text=httpRequest.getHeaders().getFirst("X-Client-Agent");
        if(text!= null){
            ClientAgent clientAgent= JSONObject.parseObject(text,ClientAgent.class);
            ClientAgentContextHolder.setClientAgentContext(clientAgent);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE+1;
    }
}
