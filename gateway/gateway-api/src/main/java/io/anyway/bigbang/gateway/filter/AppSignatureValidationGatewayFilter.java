package io.anyway.bigbang.gateway.filter;

import io.anyway.bigbang.framework.utils.JsonUtil;
import io.anyway.bigbang.gateway.utils.MacSigner;
import io.anyway.bigbang.gateway.utils.WebExchangeResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.anyway.bigbang.gateway.filter.AccessTokenValidatorGatewayFilter.accessTokenName;

@Slf4j
public class AppSignatureValidationGatewayFilter implements GatewayFilter, Ordered  {

    private volatile Map<String, MacSigner> macSignerMapping= Collections.emptyMap();

    @Value("#{${spring.cloud.gateway.client-validator.signature.verifierKeyMapping}}")
    public void setVerifierKeyMapping(Map<String,String> verifierKeyMapping){
        Map<String,MacSigner> macSignerMapping= new LinkedHashMap<>();
        for(Map.Entry<String,String> each: verifierKeyMapping.entrySet()){
            macSignerMapping.put(each.getKey(),new MacSigner(each.getValue()));
        }
        this.macSignerMapping= macSignerMapping;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        MultiValueMap<String, String> request = exchange.getRequest().getQueryParams();
        String sign = request.getFirst("sign");
        String verifierKey = request.getFirst("verifierKey");
        String timestamp = request.getFirst("timestamp");

        Assert.notNull(sign, "Signature must be not empty.");
        Assert.notNull(verifierKey, "VerifierKey must be not empty.");
        Assert.notNull(macSignerMapping.get(verifierKey),"VerifierKey value must be not empty.");
        Assert.notNull(timestamp, "Timestamp must be not empty.");

        TreeMap<String, String> content = new TreeMap<>();
        content.put("path", exchange.getRequest().getPath().value());
        HashSet<String> queryKeySet = new HashSet<>(request.keySet());
        queryKeySet.remove("sign");
        queryKeySet.remove(accessTokenName);
        for (String key : queryKeySet) {
            content.put("param_" + key, request.getFirst(key));
        }
        byte[] buffer = (byte[]) exchange.getAttributes().get(CacheRequestBodyGlobalFilter.CACHED_BODY_ATTR);
        String body = (buffer != null && buffer.length > 0) ? new String(buffer, StandardCharsets.UTF_8) : "";
        content.put("body", body);

        if (!valid(verifierKey,sign, timestamp, content)) {
            return WebExchangeResponseUtil.handleError(exchange, HttpStatus.BAD_REQUEST,"Invalid signature.");
        }
        return chain.filter(exchange);
    }

    private boolean valid(String verifierKey,String sign,String timestamp,TreeMap<String,String> content) {
        long ts= Long.parseLong(timestamp);
        long cts= System.currentTimeMillis();
        if(cts < ts && ts-cts< 5000 ) { //must be an effective request in 5 seconds .
            try {
                byte[] contentBytes= JsonUtil.fromObject2String(content).getBytes("UTF-8");
                byte[] signBytes= Base64.decodeBase64(sign);
                return macSignerMapping.get(verifierKey).verify(contentBytes,signBytes);
            } catch (Exception e) {
                log.error("HMACSHA256 verification was error", e);
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 20;
    }

}
