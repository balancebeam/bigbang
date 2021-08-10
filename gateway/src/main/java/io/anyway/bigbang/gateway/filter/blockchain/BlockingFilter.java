package io.anyway.bigbang.gateway.filter.blockchain;

import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

public interface BlockingFilter {

    void invoke(ServerWebExchange exchange,
                Map<String,String> header,
                BlockingFilterInvoker invoker);
}
