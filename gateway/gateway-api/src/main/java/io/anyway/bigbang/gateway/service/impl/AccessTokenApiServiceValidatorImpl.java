package io.anyway.bigbang.gateway.service.impl;

import io.anyway.bigbang.framework.session.DefaultUserDetailContext;
import io.anyway.bigbang.framework.session.UserDetailContext;
import io.anyway.bigbang.gateway.service.AccessTokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "spring.cloud.gateway.token-validator",name = "mode",havingValue = "api")
public class AccessTokenApiServiceValidatorImpl implements AccessTokenValidator {

    @Override
    public Optional<UserDetailContext> check(ServerWebExchange exchange) {
        DefaultUserDetailContext ctx= new DefaultUserDetailContext();
        ctx.setUserId("1");
        ctx.setAppId("app");
        ctx.setUserName("Tom");
        return Optional.of(ctx);
    }


}
