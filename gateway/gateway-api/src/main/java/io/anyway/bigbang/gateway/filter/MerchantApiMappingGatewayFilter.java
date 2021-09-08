package io.anyway.bigbang.gateway.filter;

import io.anyway.bigbang.gateway.domain.ApiMappingDefinition;
import io.anyway.bigbang.gateway.service.MerchantApiMappingDefinitionService;
import io.anyway.bigbang.gateway.utils.WebExchangeResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.*;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@Slf4j
public class MerchantApiMappingGatewayFilter implements GlobalFilter, Ordered {

    @Resource
    private MerchantApiMappingDefinitionService merchantApiMappingDefinitionService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest req = exchange.getRequest();
        MultiValueMap<String, String> request = req.getQueryParams();

        String appId = request.getFirst("app_id");
        String apiCode=request.getFirst("api_code");
        Assert.notNull(appId, "AppId must be not empty.");
        Assert.notNull(apiCode, "AppCode must be not empty.");
        Optional<ApiMappingDefinition> optional = merchantApiMappingDefinitionService.getApiMappingDefinition(apiCode);
        if(!optional.isPresent()) {
            return WebExchangeResponseUtil.handleError(exchange, HttpStatus.BAD_REQUEST,"You didn't match any api code.");
        }
        ApiMappingDefinition apiMappingDefinition= optional.get();
        String mutatedPath= req.getPath().value() + apiMappingDefinition.getPath();
        ServerHttpRequest.Builder builder= req.mutate().path(mutatedPath);
        log.info("appId: {}, apiCode: {}, mutatedPath: {}",appId,apiCode,mutatedPath);
        exchange= exchange.mutate().request(builder.build()).build();
        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, apiMappingDefinition.getRoute());
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE+1000100;
    }


}
