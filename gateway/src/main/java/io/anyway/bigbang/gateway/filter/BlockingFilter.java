package io.anyway.bigbang.gateway.filter;

import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

public interface BlockingFilter {

    void invoke(ServerWebExchange exchange,
                Map<String,String> header,
                BlockingFilterInvoker invoker);
}
