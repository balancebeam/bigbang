package io.anyway.bigbang.gateway.filter.blockchain;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.utils.RSAUtil;
import io.anyway.bigbang.gateway.filter.CacheRequestBodyGlobalFilter;
import io.anyway.bigbang.gateway.service.impl.AbstractMerchantApiRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Component
@ConditionalOnProperty(name = {"spring.cloud.gateway.merchant-validator.enabled","spring.cloud.gateway.merchant-validator.signature.enabled"},havingValue = "true")
public class MerchantSignatureValidationBlockingFilter
        extends AbstractMerchantApiRepository<String>
        implements BlockingFilter, Ordered {

    @Resource
    private CacheManager cacheManager;

    @Override
    public void invoke(ServerWebExchange exchange, Map<String, String> header, BlockingFilterInvoker invoker) {
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
        queryKeySet.remove(AccessTokenValidatorBlockingFilter.accessTokenName);
        for (String key : queryKeySet) {
            content.put("param_" + key, request.getFirst(key));
        }
        byte[] buffer = (byte[]) exchange.getAttributes().get(CacheRequestBodyGlobalFilter.CACHED_BODY_ATTR);
        String body = (buffer != null && buffer.length > 0) ? new String(buffer, StandardCharsets.UTF_8) : "";
        content.put("body", body);

        if (!valid(appId, sign, timestamp, content)) {
            throw new BlockingFilterException(HttpStatus.BAD_REQUEST, "Invalid signature.");
        }
        invoker.invoke(exchange,header);
    }

    @Override
    public int getOrder() {
        return 20;
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
}
