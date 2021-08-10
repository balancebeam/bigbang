package io.anyway.bigbang.gateway.filter.blockchain;

import io.anyway.bigbang.gateway.domain.ApiMappingDefinition;
import io.anyway.bigbang.gateway.service.MerchantApiMappingDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;
import java.util.*;

import static io.anyway.bigbang.gateway.filter.BlockingFilterChainGlobalFilter.ADDITIONAL_RAW_PATH;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "spring.cloud.gateway.merchant-validator",name="enabled",havingValue = "true")
public class MerchantApiMappingBlockingFilter implements BlockingFilter, Ordered {

    @Resource
    private MerchantApiMappingDefinitionService merchantApiMappingDefinitionService;

    @Override
    public void invoke(ServerWebExchange exchange, Map<String, String> header, BlockingFilterInvoker invoker) {
        MultiValueMap<String, String> request = exchange.getRequest().getQueryParams();

        String appId = request.getFirst("app_id");
        String apiCode=request.getFirst("api_code");
        Assert.notNull(appId, "AppId must be not empty.");
        Assert.notNull(apiCode, "AppCode must be not empty.");
        Optional<ApiMappingDefinition> optional = merchantApiMappingDefinitionService.getApiMappingDefinition(apiCode);
        if(!optional.isPresent()) {
            throw new BlockingFilterException(HttpStatus.BAD_REQUEST,"didn't match any api code.");
        }
        ApiMappingDefinition apiMappingDefinition= optional.get();
        String mutatedPath= exchange.getRequest().getPath().value() + apiMappingDefinition.getPath();
        exchange.getAttributes().put(ADDITIONAL_RAW_PATH,mutatedPath);
        exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, apiMappingDefinition.getRoute());
        invoker.invoke(exchange,header);
    }

    @Override
    public int getOrder() {
        return 100;
    }

}
