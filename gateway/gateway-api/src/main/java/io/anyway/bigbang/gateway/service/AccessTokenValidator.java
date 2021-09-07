package io.anyway.bigbang.gateway.service;

import io.anyway.bigbang.framework.session.UserDetailContext;
import org.springframework.web.server.ServerWebExchange;

import java.util.Optional;

public interface AccessTokenValidator {

    Optional<UserDetailContext> check(ServerWebExchange exchange);
}
