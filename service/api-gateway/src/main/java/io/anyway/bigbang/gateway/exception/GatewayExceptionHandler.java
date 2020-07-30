package io.anyway.bigbang.gateway.exception;

import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    //https://github.com/chenggangpro/spring-cloud-gateway-plugin/blob/2.1.SR1.x/src/main/java/pro/chenggang/plugin/springcloud/gateway/response/JsonExceptionHandler.java
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        return null;
    }
}
