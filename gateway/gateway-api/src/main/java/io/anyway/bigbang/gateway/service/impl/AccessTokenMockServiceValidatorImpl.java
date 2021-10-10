package io.anyway.bigbang.gateway.service.impl;

import io.anyway.bigbang.framework.session.DefaultUserDetailContext;
import io.anyway.bigbang.framework.session.UserDetailContext;
import io.anyway.bigbang.gateway.service.AccessTokenValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

@Slf4j
public class AccessTokenMockServiceValidatorImpl implements AccessTokenValidator {

    @Override
    public Optional<UserDetailContext> check(ServerWebExchange exchange) {
        DefaultUserDetailContext ctx= new DefaultUserDetailContext();
        ctx.setUserId("1");
        ctx.setAppId("app");
        ctx.setType("c");
        ctx.setUserName("张三丰");
        return Optional.of(ctx);
    }


}
