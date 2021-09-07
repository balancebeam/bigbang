package io.anyway.bigbang.gateway.filter;

import io.anyway.bigbang.gateway.service.impl.AbstractMerchantApiRepository;
import io.anyway.bigbang.gateway.utils.WebExchangeResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component

public class MerchantAuthenticationValidatorGatewayFilter
        extends AbstractMerchantApiRepository<List>
        implements GatewayFilter, Ordered {

    @Resource
    private CacheManager cacheManager;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        MultiValueMap<String, String> request = exchange.getRequest().getQueryParams();
        String appId = request.getFirst("app_id");
        String apiCode=request.getFirst("api_code");
        Assert.notNull(appId, "AppId must be not empty.");
        Assert.notNull(apiCode, "AppCode must be not empty.");
        if(!check(appId,apiCode)){
            return WebExchangeResponseUtil.handleError(exchange, HttpStatus.FORBIDDEN,"You didn't have privilege.");
        }
        return chain.filter(exchange);
    }

    public boolean check(String appId,String apiCode) {
        Cache cache= cacheManager.getCache("MerchantApiAuthentication");
        Cache.ValueWrapper valueWrapper= cache.get(appId);
        if(valueWrapper!= null){
            Set<String> authenticationSet= (Set)valueWrapper.get();
            return authenticationSet.contains(apiCode);
        }
        List<String> authenticationList = execute("/internal/merchant/authentication/"+appId,List.class);
        HashSet<String> authenticationSet= new HashSet<>(authenticationList);
        valueWrapper= new SimpleValueWrapper(authenticationSet);
        Cache.ValueWrapper currentValueWrapper= cache.putIfAbsent(appId,valueWrapper);
        return ((HashSet)(currentValueWrapper== null? valueWrapper.get(): currentValueWrapper.get())).contains(apiCode);
    }

    @Override
    public int getOrder() {
        return 150;
    }

}
