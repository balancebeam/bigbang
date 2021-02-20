package io.anyway.bigbang.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.security.SecurityContextHolder;
import io.anyway.bigbang.framework.security.UserDetailContext;
import io.anyway.bigbang.gateway.service.BlackListService;
import io.anyway.bigbang.gateway.service.AccessTokenValidator;
import io.anyway.bigbang.gateway.service.WhiteListService;
import io.anyway.bigbang.gateway.utils.WebExchangeResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Optional;

import static io.anyway.bigbang.framework.gray.GrayContext.GRAY_NAME;
import static io.anyway.bigbang.framework.security.UserDetailContext.USER_HEADER_NAME;

@Slf4j
@Component
public class AccessTokenValidatorFilter implements GlobalFilter, Ordered {

    @Resource
    private WhiteListService whiteListService;

    @Resource
    private BlackListService blackListService;

    @Resource
    private AccessTokenValidator tokenValidator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path= exchange.getRequest().getPath().toString();
        log.info("request uri: {}",path);
        if(blackListService.match(path)){
            log.info("the request path {} was forbidden.",path);
            return WebExchangeResponseUtil.handleError(exchange, HttpStatus.FORBIDDEN,"FORBIDDEN.");
        }
        if(!whiteListService.match(path)) {
            String accessToken = exchange.getRequest().getHeaders().getFirst("access-token");
            if (StringUtils.isEmpty(accessToken)) {
                accessToken = exchange.getRequest().getQueryParams().getFirst("access-token");
            }
            Optional<UserDetailContext> optional= tokenValidator.check(accessToken);
            if(!optional.isPresent()){
                return WebExchangeResponseUtil.handleError(exchange, HttpStatus.UNAUTHORIZED,"UNAUTHORIZED.");
            }
            UserDetailContext ctx= optional.get();
            ServerHttpRequest req = exchange.getRequest();
            ServerHttpRequest.Builder builder= req.mutate().path(req.getURI().getRawPath());
            builder.header(USER_HEADER_NAME, JSONObject.toJSONString(ctx));
            try {
                SecurityContextHolder.setUserDetailContext(ctx);
                return chain.filter(exchange.mutate().request(builder.build()).build());
            }finally {
                SecurityContextHolder.removeUserDetailContext();
            }
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE+1;
    }
}
