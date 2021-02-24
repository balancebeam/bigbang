package io.anyway.bigbang.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.security.SecurityContextHolder;
import io.anyway.bigbang.framework.security.UserDetailContext;
import io.anyway.bigbang.gateway.service.AccessTokenValidator;
import io.anyway.bigbang.gateway.service.BlackListService;
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
import java.util.Map;
import java.util.Optional;

import static io.anyway.bigbang.framework.security.UserDetailContext.USER_HEADER_NAME;

@Slf4j
@Component
public class AccessTokenValidatorBlockingFilter implements BlockingFilter, Ordered {

    @Resource
    private WhiteListService whiteListService;

    @Resource
    private BlackListService blackListService;

    @Resource
    private AccessTokenValidator tokenValidator;

    @Override
    public int getOrder() {
        return 0;
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
        if(!whiteListService.match(path)) {
            String accessToken = exchange.getRequest().getHeaders().getFirst("access-token");
            if (StringUtils.isEmpty(accessToken)) {
                accessToken = exchange.getRequest().getQueryParams().getFirst("accessToken");
            }
            Optional<UserDetailContext> optional= tokenValidator.check(accessToken);
            if(!optional.isPresent()){
                throw new BlockingFilterException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED.");
            }
            UserDetailContext ctx= optional.get();
            header.put(USER_HEADER_NAME, JSONObject.toJSONString(ctx));
            SecurityContextHolder.setUserDetailContext(ctx);
        }
        try {
            invoker.invoke(exchange,header);
        }finally {
            SecurityContextHolder.removeUserDetailContext();
        }
    }
}
