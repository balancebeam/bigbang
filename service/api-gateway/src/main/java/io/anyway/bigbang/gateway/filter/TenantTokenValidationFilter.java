package io.anyway.bigbang.gateway.filter;

import io.anyway.bigbang.framework.core.security.AuthValidationProcessor;
import io.anyway.bigbang.framework.tenant.TenantContextHolder;
import io.anyway.bigbang.framework.tenant.TenantDetail;
import io.anyway.bigbang.framework.tenant.domain.Tenant;
import io.anyway.bigbang.framework.tenant.service.TenantService;
import io.anyway.bigbang.gateway.utils.WebExchangeResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnClass(TenantDetail.class)
public class TenantTokenValidationFilter implements GlobalFilter, Ordered , AuthValidationProcessor {

    @Resource
    private TenantService tenantService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        TenantContextHolder.remove();
        String token= exchange.getRequest().getHeaders().getFirst("X-Tenant-Token");
        if(StringUtils.isEmpty(token)){
            return WebExchangeResponseUtil.handleError(exchange,HttpStatus.FORBIDDEN,"EMPTY TENANT TOKEN.");
        }
        Tenant tenant= tenantService.getTenant(token);
        if(tenant== null){
            return WebExchangeResponseUtil.handleError(exchange,HttpStatus.FORBIDDEN,"INVALID TENANT TOKEN.");
        }
        log.info("--------------------x {}",Thread.currentThread().getId());
        TenantContextHolder.setTenantDetail(new TenantDetail(tenant.getName()));
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -201;
    }

    @Override
    public void valid(Map<String,Object> map) throws Exception {
        log.info("--------------------y {}",Thread.currentThread().getId());
        String reqUserTenant= "";
        TenantDetail tenantDetail= TenantContextHolder.getTenantDetail();
        if(tenantDetail!=null) {
            reqUserTenant= tenantDetail.getTenantId();
        }
        String authUserTenant = (String)map.get("tenant_id");
        if(authUserTenant==null){
            authUserTenant= "";
        }
        if(!reqUserTenant.equals(authUserTenant)){
            throw new Exception("Tenant was manipulated, auth-user-tenant: "+reqUserTenant+",req-user-tenant: "+reqUserTenant);
        }
    }
}
