package io.anyway.bigbang.gateway.utils;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.core.rest.RestHeader;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.util.Arrays;

public interface WebExchangeResponseUtil {

    static Mono<Void> handleError(ServerWebExchange exchange,
                                  HttpStatus status,
                                  String message){
        ServerHttpResponse response= exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().setAcceptCharset(Arrays.asList(Charset.forName("UTF-8")));
        RestHeader header= new RestHeader();
        header.setErrorCode(status.value());
        header.setMsg(message);
        String text= JSONObject.toJSONString(header);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(text.getBytes())));
    }
}
