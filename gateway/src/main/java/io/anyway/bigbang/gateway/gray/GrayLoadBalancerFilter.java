package io.anyway.bigbang.gateway.gray;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.gray.GrayContext;
import io.anyway.bigbang.framework.gray.GrayContextHolder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerUriTools;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.support.DelegatingServiceInstance;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.anyway.bigbang.framework.gray.GrayContext.GRAY_NAME;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.*;

public class GrayLoadBalancerFilter implements GlobalFilter, Ordered {

    private static final Log log = LogFactory.getLog(ReactiveLoadBalancerClientFilter.class);
    private static final int LOAD_BALANCER_CLIENT_FILTER_ORDER = 10150;
    private final LoadBalancerClientFactory clientFactory;
    private LoadBalancerProperties properties;

    private ConcurrentHashMap<String, GrayLoadBalancer> grayLoadBalancerMap= new ConcurrentHashMap<>();

    @Resource
    private GrayRibbonRule grayRibbonRule;

    @Resource
    private GrayStrategyProcessor grayStrategyProcessor;

    public GrayLoadBalancerFilter(LoadBalancerClientFactory clientFactory, LoadBalancerProperties properties) {
        this.clientFactory = clientFactory;
        this.properties = properties;
    }

    @Override
    public int getOrder() {
        return LOAD_BALANCER_CLIENT_FILTER_ORDER-1;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public Mono<Void> filter(ServerWebExchange exch, GatewayFilterChain chain) {
        URI url = exch.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        String schemePrefix = exch.getAttribute(GATEWAY_SCHEME_PREFIX_ATTR);
        if (url == null
                || (!"lb".equals(url.getScheme()) && !"lb".equals(schemePrefix))) {
            return chain.filter(exch);
        }

        ServerWebExchange exchange= makeupGrayWebExchange(exch);
        // preserve the original url
        addOriginalRequestUrl(exchange, url);

        if (log.isTraceEnabled()) {
            log.trace(ReactiveLoadBalancerClientFilter.class.getSimpleName()
                    + " url before: " + url);
        }

        return choose(exchange).doOnNext(response -> {

            if (!response.hasServer()) {
                throw NotFoundException.create(properties.isUse404(),
                        "Unable to find instance for " + url.getHost());
            }

            URI uri = exchange.getRequest().getURI();

            // if the `lb:<scheme>` mechanism was used, use `<scheme>` as the default,
            // if the loadbalancer doesn't provide one.
            String overrideScheme = null;
            if (schemePrefix != null) {
                overrideScheme = url.getScheme();
            }

            DelegatingServiceInstance serviceInstance = new DelegatingServiceInstance(
                    response.getServer(), overrideScheme);

            URI requestUrl = reconstructURI(serviceInstance, uri);

            if (log.isTraceEnabled()) {
                log.trace("LoadBalancerClientFilter url chosen: " + requestUrl);
            }
            exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);
        }).then(chain.filter(exchange));
    }

    protected URI reconstructURI(ServiceInstance serviceInstance, URI original) {
        return LoadBalancerUriTools.reconstructURI(serviceInstance, original);
    }

    private Mono<Response<ServiceInstance>> choose(ServerWebExchange exchange) {
        URI uri = (URI) exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
        try {
            if(grayLoadBalancerMap.contains( uri.getHost())){
                GrayLoadBalancer loadBalancer = grayLoadBalancerMap.get( uri.getHost());
                return loadBalancer.choose(this.createRequest(exchange));
            }
            ObjectProvider<ServiceInstanceListSupplier> provider= clientFactory.getLazyProvider(uri.getHost(), ServiceInstanceListSupplier.class);
            if(provider == null){
                throw new NotFoundException("No loadbalancer available for " + uri.getHost());
            }
            grayLoadBalancerMap.putIfAbsent(uri.getHost(),new GrayLoadBalancer(provider,grayRibbonRule,uri.getHost()));
            GrayLoadBalancer loadBalancer = grayLoadBalancerMap.get( uri.getHost());
            return loadBalancer.choose(this.createRequest(exchange));
        }
        finally {
            GrayContextHolder.removeGrayContext();
        }
    }

    private Request createRequest(ServerWebExchange exchange) {
        Optional<GrayContext> optional= GrayContextHolder.getGrayContext();
        Request<Optional<GrayContext>> request = new DefaultRequest<>(optional);
        return request;
    }

    private ServerWebExchange makeupGrayWebExchange(ServerWebExchange exchange){
        GrayContext ctx= grayStrategyProcessor.invoke(exchange);
        if(ctx!= null){
            GrayContextHolder.setGrayContext(ctx);
            ServerHttpRequest req = exchange.getRequest();
            ServerHttpRequest.Builder builder= req.mutate().path(req.getURI().getRawPath());
            builder.header(GRAY_NAME,JSONObject.toJSONString(ctx));
            return exchange.mutate().request(builder.build()).build();
        }
        return exchange;
    }

}
