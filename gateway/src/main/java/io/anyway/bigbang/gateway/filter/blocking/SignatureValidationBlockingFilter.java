package io.anyway.bigbang.gateway.filter.blocking;

import io.anyway.bigbang.framework.session.SessionContextHolder;
import io.anyway.bigbang.framework.session.UserDetailContext;
import io.anyway.bigbang.gateway.service.SignatureValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static io.anyway.bigbang.gateway.filter.CacheRequestBodyGlobalFilter.CACHED_BODY_ATTR;


@Slf4j
@Component
@ConditionalOnProperty(prefix = "spring.cloud.gateway.signature-validator",name="enabled",havingValue = "true")
public class SignatureValidationBlockingFilter implements BlockingFilter, Ordered {

    @Resource
    private SignatureValidationService signatureValidationService;

    @Override
    public void invoke(ServerWebExchange exchange, Map<String, String> header, BlockingFilterInvoker invoker) {
        Optional<UserDetailContext> userDetailContext= SessionContextHolder.getUserDetailContext();
        if(userDetailContext.isPresent()) {
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
            queryKeySet.remove("access_token");
            for (String key : queryKeySet) {
                content.put("param_" + key, request.getFirst(key));
            }
            byte[] buffer = (byte[]) exchange.getAttributes().get(CACHED_BODY_ATTR);
            String body = (buffer != null && buffer.length > 0) ? new String(buffer, StandardCharsets.UTF_8) : "";
            content.put("body", body);

            if (!signatureValidationService.valid(appId, sign, timestamp, content)) {
                throw new BlockingFilterException(HttpStatus.BAD_REQUEST, "Invalid signature.");
            }
        }
        invoker.invoke(exchange,header);
    }

    @Override
    public int getOrder() {
        return 20;
    }
}
