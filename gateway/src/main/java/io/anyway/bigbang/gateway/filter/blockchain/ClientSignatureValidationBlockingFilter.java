package io.anyway.bigbang.gateway.filter.blockchain;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.gateway.filter.CacheRequestBodyGlobalFilter;
import io.anyway.bigbang.gateway.utils.MacSigner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.anyway.bigbang.gateway.filter.blockchain.AccessTokenValidatorBlockingFilter.accessTokenName;

@Slf4j
@Component
@ConditionalOnProperty(name = {"spring.cloud.gateway.client-validator.enabled","spring.cloud.gateway.client-validator.signature.enabled"},havingValue = "true")
public class ClientSignatureValidationBlockingFilter implements BlockingFilter, Ordered  {

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
    public void invoke(ServerWebExchange exchange, Map<String, String> header, BlockingFilterInvoker invoker) {
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
            throw new BlockingFilterException(HttpStatus.BAD_REQUEST, "Invalid signature.");
        }
        invoker.invoke(exchange,header);
    }

    @Override
    public int getOrder() {
        return 20;
    }

    private boolean valid(String verifierKey,String sign,String timestamp,TreeMap<String,String> content) {
        long ts= Long.parseLong(timestamp);
        long cts= System.currentTimeMillis();
        if(cts < ts && ts-cts< 5000 ) { //must be an effective request in 5 seconds .
            try {
                byte[] contentBytes= JSONObject.toJSONString(content).getBytes("UTF-8");
                byte[] signBytes= Base64.decodeBase64(sign);
                return macSignerMapping.get(verifierKey).verify(contentBytes,signBytes);
            } catch (Exception e) {
                log.error("HMACSHA256 verification was error", e);
            }
        }
        return false;
    }

}
