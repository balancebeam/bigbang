package io.anyway.bigbang.gateway.filter.blockchain;

import io.anyway.bigbang.framework.session.SessionContextHolder;
import io.anyway.bigbang.framework.session.UserDetailContext;
import io.anyway.bigbang.framework.utils.JsonUtil;
import io.anyway.bigbang.gateway.service.AccessTokenValidator;
import io.anyway.bigbang.gateway.service.RequestPathBlackListService;
import io.anyway.bigbang.gateway.service.RequestPathWhiteListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;

import static io.anyway.bigbang.framework.session.UserDetailContext.USER_HEADER_NAME;

@Slf4j
@Component
public class AccessTokenValidatorBlockingFilter implements BlockingFilter, Ordered {

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
    public int getOrder() {
        return 50;
    }

    @Override
    public void invoke(ServerWebExchange exchange,
                       Map<String, String> header,
                       BlockingFilterInvoker invoker) {
        String path= exchange.getRequest().getPath().toString();
        log.info("request uri: {}",path);
        if(blackListService.match(path)){
            log.info("the request path {} was forbidden.",path);
            throw new BlockingFilterException(HttpStatus.FORBIDDEN,"FORBIDDEN.");
        }
        if(!enabled || !whiteListService.match(path)) {
            Optional<UserDetailContext> optional= tokenValidator.check(exchange);
            if(!optional.isPresent()){
                throw new BlockingFilterException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED.");
            }
            UserDetailContext ctx= optional.get();
//            if(!ctx.getAppId().equals(exchange.getRequest().getQueryParams().getFirst("app_id"))){
//                throw new RuntimeException("Invalid application identify (app_id) request.");
//            }
            header.put(USER_HEADER_NAME, JsonUtil.fromObject2String(ctx));
            SessionContextHolder.setUserDetailContext(ctx);
        }
        try {
            invoker.invoke(exchange,header);
        }finally {
            SessionContextHolder.removeUserDetailContext();
        }
    }
}
