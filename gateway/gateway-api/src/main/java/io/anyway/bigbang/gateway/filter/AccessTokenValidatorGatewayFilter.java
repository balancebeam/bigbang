package io.anyway.bigbang.gateway.filter;

import io.anyway.bigbang.framework.session.UserDetailContext;
import io.anyway.bigbang.framework.utils.JsonUtil;
import io.anyway.bigbang.gateway.service.AccessTokenValidator;
import io.anyway.bigbang.gateway.service.RequestPathBlackListService;
import io.anyway.bigbang.gateway.service.RequestPathWhiteListService;
import io.anyway.bigbang.gateway.utils.WebExchangeResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Optional;

import static io.anyway.bigbang.framework.session.UserDetailContext.USER_HEADER_NAME;

@Slf4j
public class AccessTokenValidatorGatewayFilter implements GatewayFilter, Ordered {

    @Resource
    private RequestPathWhiteListService whiteListService;

    @Resource
    private RequestPathBlackListService blackListService;

    @Resource
    private AccessTokenValidator tokenValidator;

    @Value("${spring.cloud.gateway.token-validator.enabled:true}")
    private boolean enabled;

    final public static String accessTokenName = "access_token";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req= exchange.getRequest();
        String path= req.getPath().toString();
        log.info("request uri: {}",path);
        if(blackListService.match(path)){
            log.info("the request path {} was forbidden.",path);
            return WebExchangeResponseUtil.handleError(exchange, HttpStatus.FORBIDDEN,"FORBIDDEN.");
        }
        if(!enabled || !whiteListService.match(path)) {
            Optional<UserDetailContext> optional= tokenValidator.check(exchange);
            if(!optional.isPresent()){
                return WebExchangeResponseUtil.handleError(exchange, HttpStatus.UNAUTHORIZED,"UNAUTHORIZED.");
            }
            UserDetailContext ctx= optional.get();
            ServerHttpRequest request = exchange.getRequest().mutate()
                    .header(USER_HEADER_NAME, JsonUtil.fromObject2String(ctx))
                    .build();

            exchange= exchange.mutate().request(request).build();
            exchange.getAttributes().put(USER_HEADER_NAME,ctx);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 50;
    }
}
