package io.anyway.bigbang.gateway.filter.blocking;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.session.SessionContextHolder;
import io.anyway.bigbang.framework.session.UserDetailContext;
import io.anyway.bigbang.gateway.service.AccessTokenValidator;
import io.anyway.bigbang.gateway.service.BlackListService;
import io.anyway.bigbang.gateway.service.WhiteListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;

import static io.anyway.bigbang.framework.session.UserDetailContext.USER_HEADER_NAME;


@Slf4j
@Component
@ConditionalOnProperty(prefix = "spring.cloud.gateway.token-validator",name="enabled",havingValue = "true")
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
            SessionContextHolder.setUserDetailContext(ctx);
        }
        try {
            invoker.invoke(exchange,header);
        }finally {
            SessionContextHolder.removeUserDetailContext();
        }
    }
}
