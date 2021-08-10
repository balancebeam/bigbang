package io.anyway.bigbang.gateway.filter;

import com.alibaba.ttl.TransmittableThreadLocal;
import io.anyway.bigbang.gateway.filter.blockchain.BlockingFilter;
import io.anyway.bigbang.gateway.filter.blockchain.BlockingFilterException;
import io.anyway.bigbang.gateway.filter.blockchain.BlockingFilterInvoker;
import io.anyway.bigbang.gateway.utils.WebExchangeResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BlockingFilterChainGlobalFilter implements GlobalFilter, Ordered, InitializingBean {

    final public static ThreadLocal<Integer> FILTER_CURSOR = new TransmittableThreadLocal<>();

    @Resource
    private List<BlockingFilter> blockingFilters= Collections.EMPTY_LIST;

    private BlockingFilterInvoker invoker;

    final public static String ADDITIONAL_RAW_PATH = "additionalRawPath";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        try {
            FILTER_CURSOR.set(0);
            Map<String,String> header= new HashMap<>();
            invoker.invoke(exchange,header);
            ServerHttpRequest req = exchange.getRequest();
            String replacePath= (String)exchange.getAttributes().get(ADDITIONAL_RAW_PATH);
            String rawPath= StringUtils.hasLength(replacePath)? replacePath : req.getURI().getRawPath();
            ServerHttpRequest.Builder builder= req.mutate().path(rawPath);
            for(String key: header.keySet()){
                builder.header(key, header.get(key));
            }
            exchange= exchange.mutate().request(builder.build()).build();
        }catch (BlockingFilterException e){
            return WebExchangeResponseUtil.handleError(exchange, e.getHttpStatus(),e.getMessage());
        }catch (Exception e){
            return WebExchangeResponseUtil.handleError(exchange, HttpStatus.BAD_GATEWAY,e.getMessage());
        }
        finally {
            FILTER_CURSOR.remove();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE+1001;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        invoker= new BlockingFilterInvoker(){
            private int size= blockingFilters.size();
            @Override
            public void invoke(ServerWebExchange exchange, Map<String,String> header) {
                int index= FILTER_CURSOR.get();
                if(index < size){
                    FILTER_CURSOR.set(index+1);
                    blockingFilters.get(index).invoke(exchange,header,this);
                }
            }
        };
    }

}
