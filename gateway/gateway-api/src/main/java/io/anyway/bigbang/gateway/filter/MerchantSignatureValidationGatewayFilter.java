package io.anyway.bigbang.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.utils.RSAUtil;
import io.anyway.bigbang.gateway.service.impl.AbstractMerchantApiRepository;
import io.anyway.bigbang.gateway.utils.WebExchangeResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.HashSet;
import java.util.TreeMap;

@Slf4j
public class MerchantSignatureValidationGatewayFilter
        extends AbstractMerchantApiRepository<String>
        implements GlobalFilter, Ordered {

    @Resource
    private CacheManager cacheManager;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        MultiValueMap<String, String> request = exchange.getRequest().getQueryParams();
        String sign = request.getFirst("sign");
        String appId = request.getFirst("app_id");
        String timestamp = request.getFirst("timestamp");

        Assert.notNull(sign, "Signature must be not empty.");
        Assert.notNull(appId, "AppId must be not empty.");
        Assert.notNull(timestamp, "Timestamp must be not empty.");

        TreeMap<String, String> content = new TreeMap<>();
        content.put("path", exchange.getRequest().getPath().value());
        HashSet<String> queryKeySet = new HashSet<>(request.keySet());
        queryKeySet.remove("sign");
        queryKeySet.remove(AccessTokenValidatorGatewayFilter.accessTokenName);
        for (String key : queryKeySet) {
            content.put("param_" + key, request.getFirst(key));
        }
        byte[] buffer = (byte[]) exchange.getAttributes().get(CacheRequestBodyGlobalFilter.CACHED_BODY_ATTR);
        String body = (buffer != null && buffer.length > 0) ? new String(buffer, StandardCharsets.UTF_8) : "";
        content.put("body", body);
        if (!valid(appId, sign, timestamp, content)) {
            return WebExchangeResponseUtil.handleError(exchange, HttpStatus.BAD_REQUEST,"Invalid signature.");
        }
        return chain.filter(exchange);
    }

    private boolean valid(String appId,String sign,String timestamp,TreeMap<String,String> content) {
        long ts= Long.parseLong(timestamp);
        long cts= System.currentTimeMillis();
        if(cts < ts && ts-cts< 5000 ) { //must be an effective request in 5 seconds .
            try {
                return RSAUtil.verifySign(getPublicKey(appId),JSONObject.toJSONString(content).getBytes("UTF-8"),sign);
            } catch (Exception e) {
                log.error("rsa verification was error", e);
            }
        }
        return false;
    }

    private RSAPublicKey getPublicKey(String appId){
        Cache cache= cacheManager.getCache("MerchantPublicKey");
        Cache.ValueWrapper valueWrapper= cache.get(appId);
        if(valueWrapper!= null){
            return (RSAPublicKey)valueWrapper.get();
        }
        String publicKeyString = execute("/internal/merchant/public-key/"+appId,String.class);
        RSAPublicKey publicKey= RSAUtil.decodeToPublicKey(publicKeyString);
        if(publicKey== null){
            throw new RuntimeException(appId+" public key was invalid");
        }
        valueWrapper= new SimpleValueWrapper(publicKey);
        Cache.ValueWrapper currentValueWrapper= cache.putIfAbsent(appId,valueWrapper);
        return (RSAPublicKey)(currentValueWrapper== null? valueWrapper.get(): currentValueWrapper.get());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE+100020;
    }

}
