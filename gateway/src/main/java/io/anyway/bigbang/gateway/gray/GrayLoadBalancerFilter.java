package io.anyway.bigbang.gateway.gray;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.discovery.GrayRouteContext;
import io.anyway.bigbang.framework.discovery.GrayRouteContextHolder;
import io.anyway.bigbang.framework.security.UserDetailContext;
import io.anyway.bigbang.framework.useragent.UserAgentContext;
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
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static io.anyway.bigbang.framework.discovery.GrayRouteContext.GRAY_ROUTE_NAME;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.*;

public class GrayLoadBalancerFilter implements GlobalFilter, Ordered, GrayRouteListener {

    private static final Log log = LogFactory.getLog(ReactiveLoadBalancerClientFilter.class);
    private static final int LOAD_BALANCER_CLIENT_FILTER_ORDER = 10150;
    private final LoadBalancerClientFactory clientFactory;
    private LoadBalancerProperties properties;
    private Random random= new Random();
    private volatile GrayRouteStrategy strategy= new GrayRouteStrategy();
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
        try {
            return loadBalancer.choose(this.createRequest(exchange));
        }
        finally {
            GrayRouteContextHolder.removeGrayRouteContext();
        }
    }

    private Request createRequest(ServerWebExchange exchange) {
        HttpHeaders headers = exchange.getRequest().getHeaders();
        Request<HttpHeaders> request = new DefaultRequest<>(headers);
        return request;
    }

    private ServerWebExchange makeupGrayWebExchange(ServerWebExchange exchange){
        HttpHeaders headers= exchange.getRequest().getHeaders();
        String text= headers.getFirst(GRAY_ROUTE_NAME);
        if(!StringUtils.isEmpty(text)){
            GrayRouteContext context= JSONObject.parseObject(text, GrayRouteContext.class);
            return buildNewExchange(exchange,context);
        }
        //use uat tester
        List<GrayRouteStrategy.TesterDefinition> uatList= strategy.getUatList();
        if(!uatList.isEmpty()){
            String detail= headers.getFirst(UserDetailContext.USER_HEADER_NAME);
            if(!StringUtils.isEmpty(detail)){
                UserDetailContext userDetail= JSONObject.parseObject(detail, UserDetailContext.class);
                if(Objects.isNull(userDetail.getUid())){
                    userDetail.setUid(Long.parseLong(detail));
                }
                String candidate= "usr_"+userDetail.getType()+"_"+userDetail.getUid();
                for(GrayRouteStrategy.TesterDefinition each: uatList){
                    for(Pattern user: each.getTesters()){
                        if(user.matcher(candidate).find()){
                            return buildNewExchange(exchange, each);
                        }
                    }
                }
            }
            detail= headers.getFirst(UserAgentContext.USER_AGENT_NAME);
            if(!StringUtils.isEmpty(detail)){
                UserAgentContext userAgent= JSONObject.parseObject(detail, UserAgentContext.class);
                String candidate= "cli_"+userAgent.getPlatform()+"_"+userAgent.getVersion();
                for(GrayRouteStrategy.TesterDefinition each: uatList){
                    for(Pattern user: each.getTesters()){
                        if(user.matcher(candidate).find()){
                            return buildNewExchange(exchange, each);
                        }
                    }
                }
            }
        }
        //use random weight
        List<GrayRouteStrategy.WeightDefinition> wgtList= strategy.getWgtList();
        if(!wgtList.isEmpty()){
            int total= 0;
            for(GrayRouteStrategy.WeightDefinition each: wgtList){
                total+=each.getWeight();
            }
            int rdm= random.nextInt(total);
            int sum= 0;
            for(GrayRouteStrategy.WeightDefinition each: wgtList){
                sum+= each.getWeight();
                if(sum>rdm){
                    return buildNewExchange(exchange,each);
                }
            }
        }
        //use the default route context
        GrayRouteContext ctx= strategy.getDefaultContext();
        if(ctx!= null){
            if(StringUtils.isEmpty(ctx.getGroup())){
                ctx.setGroup("DEFAULT_GROUP");
            }
            if(StringUtils.isEmpty(ctx.getClusterName())){
                ctx.setGroup("DEFAULT");
            }
            return buildNewExchange(exchange, ctx);
        }
        return exchange;
    }

    private ServerWebExchange buildNewExchange(ServerWebExchange exchange,GrayRouteContext ctx){
        if(StringUtils.isEmpty(ctx.getGroup()) || StringUtils.isEmpty(ctx.getClusterName())){
            throw new IllegalArgumentException("group or clusterName was empty.");
        }
        GrayRouteContextHolder.setGrayRouteContext(ctx);
        ServerHttpRequest req = exchange.getRequest();
        ServerHttpRequest.Builder builder= req.mutate().path(req.getURI().getRawPath());
        builder.header(GRAY_ROUTE_NAME,JSONObject.toJSONString(ctx));
        return exchange.mutate().request(builder.build()).build();
    }

    @Override
    public void onRouteChange(GrayRouteStrategy strategy) {
        this.strategy= strategy;
    }

}
