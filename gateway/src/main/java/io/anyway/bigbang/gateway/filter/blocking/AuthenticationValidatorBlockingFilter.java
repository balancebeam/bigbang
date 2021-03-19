package io.anyway.bigbang.gateway.filter.blocking;


import io.anyway.bigbang.gateway.domain.ApiResource;
import io.anyway.bigbang.gateway.service.ApiPrivilegeValidatorService;
import io.anyway.bigbang.gateway.service.ApiResourceDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;

import static io.anyway.bigbang.gateway.filter.BlockingFilterChainGlobalFilter.ADDITIONAL_RAW_PATH;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "spring.cloud.gateway.authentication-validator",name="enabled",havingValue = "true")
public class AuthenticationValidatorBlockingFilter implements BlockingFilter, Ordered {

    @Resource
    private ApiResourceDefinitionService apiResourceService;

    @Resource
    private ApiPrivilegeValidatorService apiPrivilegeService;

    @Override
    public void invoke(ServerWebExchange exchange, Map<String, String> header, BlockingFilterInvoker invoker) {
        String apiCode= exchange.getRequest().getQueryParams().getFirst("api_code");
        if(!StringUtils.isEmpty(apiCode)){
            if(!apiPrivilegeService.permit(apiCode)){
                throw new BlockingFilterException(HttpStatus.FORBIDDEN,"You didn't have privilege.");
            }
            Optional<ApiResource> optional = apiResourceService.getApiResource(apiCode);
            if(!optional.isPresent()) {
                throw new BlockingFilterException(HttpStatus.BAD_REQUEST,"didn't match any api service.");
            }
            ApiResource apiResource= optional.get();
            String mutatedPath= exchange.getRequest().getPath().value()+apiResource.getPath();
            if(apiResource.getMatcher()!= null &&
                    !apiResource.getMatcher().matcher(mutatedPath).matches()){
                throw new BlockingFilterException(HttpStatus.BAD_REQUEST,"Invalid request path.");
            }
            exchange.getAttributes().put(ADDITIONAL_RAW_PATH,mutatedPath);
            exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, apiResource.getRoute());
        }
        invoker.invoke(exchange,header);
    }

    @Override
    public int getOrder() {
        return 100;
    }


}
