package io.anyway.bigbang.gateway.gray;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.kernel.security.UserDetail;
import io.anyway.bigbang.framework.kernel.useragent.UserAgent;
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

import java.net.URI;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.*;

public class GrayLoadBalancerFilter implements GlobalFilter, Ordered, GrayRouteListener {

    private static final Log log = LogFactory.getLog(ReactiveLoadBalancerClientFilter.class);
    private static final int LOAD_BALANCER_CLIENT_FILTER_ORDER = 10150;
    private final LoadBalancerClientFactory clientFactory;
    private LoadBalancerProperties properties;
    private Random random= new Random();
    private AtomicReference<GrayRouteStrategy> strategyRef= new AtomicReference<>(new GrayRouteStrategy());
    private ConcurrentHashMap<String, GrayLoadBalancer> grayLoadBalancerMap= new ConcurrentHashMap<>();

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
        if(grayLoadBalancerMap.contains( uri.getHost())){
            GrayLoadBalancer loadBalancer = grayLoadBalancerMap.get( uri.getHost());
            return loadBalancer.choose(this.createRequest(exchange));
        }
        ObjectProvider<ServiceInstanceListSupplier> provider= clientFactory.getLazyProvider(uri.getHost(), ServiceInstanceListSupplier.class);
        if(provider == null){
            throw new NotFoundException("No loadbalancer available for " + uri.getHost());
        }
        grayLoadBalancerMap.putIfAbsent(uri.getHost(),new GrayLoadBalancer(provider,uri.getHost()));
        GrayLoadBalancer loadBalancer = grayLoadBalancerMap.get( uri.getHost());
        return loadBalancer.choose(this.createRequest(exchange));
    }

    private Request createRequest(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        Request<HttpHeaders> request = new DefaultRequest<>(headers);
        return request;
    }

    private ServerWebExchange makeupGrayWebExchange(ServerWebExchange exchange){
        HttpHeaders headers= exchange.getRequest().getHeaders();
        String unit= headers.getFirst(GRAY_UNIT_NAME);
        if(!StringUtils.isEmpty(unit)){
            return exchange;
        }
        Map<Pattern,String> operator= strategyRef.get().getOperator();
        if(!operator.isEmpty()){
            String detail= headers.getFirst(UserDetail.USER_HEADER_NAME);
            if(!StringUtils.isEmpty(detail)){
                UserDetail userDetail= JSONObject.parseObject(detail, UserDetail.class);
                String candidate= "usr_"+userDetail.getType()+"_"+userDetail.getUid();
                for(Pattern each: operator.keySet()){
                    if(each.matcher(candidate).find()){
                        return buildNewExchange(exchange,operator.get(each));
                    }
                }
            }
            detail= headers.getFirst(UserAgent.USER_AGENT_NAME);
            if(!StringUtils.isEmpty(detail)){
                UserAgent userAgent= JSONObject.parseObject(detail, UserAgent.class);
                String candidate= "cli_"+userAgent.getPlatform()+"_"+userAgent.getVersion();
                for(Pattern each: operator.keySet()){
                    if(each.matcher(candidate).find()){
                        return buildNewExchange(exchange,operator.get(each));
                    }
                }
            }
            String defaultValue= headers.getFirst(GRAY_INDICATOR_DEFAULT_VALUE_NAME);
            if(StringUtils.isEmpty(defaultValue)){
                defaultValue= GRAY_DEFAULT_UNIT;
            }
            return buildNewExchange(exchange,defaultValue);
        }
        Map<String,Integer> weight= strategyRef.get().getWeight();
        if(!weight.isEmpty()){
            int total= weight.values().stream().reduce((a1, a2) -> a1+a2).get();
            int rdm= random.nextInt(total);
            int sum= 0;
            for(Map.Entry<String,Integer> each: weight.entrySet()){
                sum+= each.getValue();
                if(sum>rdm){
                    return buildNewExchange(exchange,each.getKey());
                }
            }
        }
        return exchange;
    }

    private ServerWebExchange buildNewExchange(ServerWebExchange exchange,String unit){
        ServerHttpRequest req = exchange.getRequest();
        ServerHttpRequest.Builder builder= req.mutate().path(req.getURI().getRawPath());
        builder.header(GRAY_UNIT_NAME,unit);
        return exchange.mutate().request(builder.build()).build();
    }

    @Override
    public void onRouteChange(GrayRouteStrategy strategy) {
        strategyRef.set(strategy);
    }

}
