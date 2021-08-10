package io.anyway.bigbang.gateway.filter;

import io.netty.buffer.EmptyByteBuf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@Component
@ConditionalOnProperty(prefix = "spring.cloud.gateway.validator.signature",name="enabled",havingValue = "true")
public class CacheRequestBodyGlobalFilter implements GlobalFilter, Ordered {

    final public static String CACHED_BODY_ATTR = "cachedBody";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String contentType= exchange.getRequest().getHeaders().getFirst("content-type");
        //if it uploaded file,then didn't cache body data
        if(contentType!= null && contentType.contains("multipart/form-data;")){
            return chain.filter(exchange);
        }
        ServerHttpResponse response = exchange.getResponse();
        NettyDataBufferFactory factory = (NettyDataBufferFactory) response
                .bufferFactory();

        return DataBufferUtils.join(exchange.getRequest().getBody())
            .defaultIfEmpty(factory.wrap(new EmptyByteBuf(factory.getByteBufAllocator())))
            .flatMap(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
                    DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                    DataBufferUtils.retain(buffer);
                    return Mono.just(buffer);
                });
                ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return cachedFlux;
                    }
                };
                ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                mutatedExchange.getAttributes().put(CACHED_BODY_ATTR, bytes);
                return chain.filter(mutatedExchange);
            });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
