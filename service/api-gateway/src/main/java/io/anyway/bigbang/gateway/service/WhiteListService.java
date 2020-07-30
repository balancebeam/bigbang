package io.anyway.bigbang.gateway.service;

import org.springframework.web.server.ServerWebExchange;

public interface WhiteListService {

    boolean isWhitelistApi(ServerWebExchange exchange);
}
