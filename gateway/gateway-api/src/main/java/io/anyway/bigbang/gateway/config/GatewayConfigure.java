package io.anyway.bigbang.gateway.config;

import io.anyway.bigbang.framework.bootstrap.config.RestTemplateConfigure;
import io.anyway.bigbang.gateway.controller.GatewayController;
import io.anyway.bigbang.gateway.exception.GatewayExceptionHandler;
import io.anyway.bigbang.gateway.filter.*;
import io.anyway.bigbang.gateway.service.*;
import io.anyway.bigbang.gateway.service.impl.*;
import io.anyway.bigbang.gateway.swagger.SwaggerAggregationResourcesProvider;
import io.anyway.bigbang.gateway.swagger.SwaggerController;
import io.anyway.bigbang.gateway.swagger.SwaggerProxyGlobalFilter;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@RefreshScope
@Configuration
@ImportAutoConfiguration({
        RestTemplateConfigure.class,
        CacheConfigure.class,
        DynamicRouteConfig.class,
        GrayRouteConfig.class,
        RequestPathConfigure.class,
        GatewayExceptionHandler.class
})
public class GatewayConfigure {

    @Bean
    public DynamicRouteService createDynamicRouteService(){
        return new DynamicRouteService();
    }

    @Bean
    public GatewayController createGatewayController(){
        return new GatewayController();
    }

    @Bean
    public HttpEndpointMetricGlobalFilter createHttpEndpointMetricGlobalFilter(){
        return new HttpEndpointMetricGlobalFilter();
    }

    @Bean
    public CacheRequestBodyGlobalFilter createCacheRequestBodyGlobalFilter(){
        return new CacheRequestBodyGlobalFilter();
    }

    @Bean
    public AccessTokenValidatorGatewayFilter createAccessTokenValidatorGatewayFilter(){
        return new AccessTokenValidatorGatewayFilter();
    }

    @Bean
    @ConditionalOnProperty(name = {"spring.cloud.gateway.app-validator.enabled","spring.cloud.gateway.app-validator.signature.enabled"},havingValue = "true")
    public AppSignatureValidationGatewayFilter createAppSignatureValidationGatewayFilter(){
        return new AppSignatureValidationGatewayFilter();
    }

    @Bean
    @ConditionalOnProperty(name = {"spring.cloud.gateway.merchant-validator.enabled","spring.cloud.gateway.merchant-validator.signature.enabled"},havingValue = "true")
    public MerchantSignatureValidationGatewayFilter createMerchantSignatureValidationGatewayFilter(){
        return new MerchantSignatureValidationGatewayFilter();
    }

    @Bean
    @ConditionalOnProperty(name = {"spring.cloud.gateway.merchant-validator.enabled","spring.cloud.gateway.merchant-validator.authentication.enabled"},havingValue = "true")
    public MerchantAuthenticationValidatorGatewayFilter createMerchantAuthenticationValidatorGatewayFilter(){
        return new MerchantAuthenticationValidatorGatewayFilter();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.cloud.gateway.merchant-validator",name="enabled",havingValue = "true")
    public MerchantApiMappingGatewayFilter createMerchantApiMappingGatewayFilter(){
        return new MerchantApiMappingGatewayFilter();
    }

    @Bean
    public RequestPathWhiteListService createRequestPathWhiteListService(){
        return new RequestPathWhiteListServiceImpl();
    }

    @Bean
    public RequestPathBlackListService createRequestPathBlackListService(){
        return new RequestPathBlackListServiceImpl();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.cloud.gateway.merchant-validator",name="enabled",havingValue = "true")
    public MerchantApiMappingDefinitionService createMerchantApiMappingDefinitionServiceImpl(){
        return new MerchantApiMappingDefinitionServiceImpl();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.cloud.gateway.token-validator",name = "mode",havingValue = "redis")
    public AccessTokenValidator createAccessTokenOAuth2RedisValidatorImpl(){
        return new AccessTokenOAuth2RedisValidatorImpl();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.cloud.gateway.token-validator",name = "mode",havingValue = "jwt")
    public AccessTokenValidator createAccessTokenOAuth2JwtValidatorImpl(){
        return new AccessTokenOAuth2JwtValidatorImpl();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.cloud.gateway.token-validator",name = "mode",havingValue = "mock")
    public AccessTokenValidator createAccessTokenMockServiceValidatorImpl(){
        return new AccessTokenMockServiceValidatorImpl();
    }

    @Bean
    public SwaggerAggregationResourcesProvider createSwaggerAggregationResourcesProvider(){
        return new SwaggerAggregationResourcesProvider();
    }

    @Bean
    public SwaggerController createSwaggerController(){
        return new SwaggerController();
    }

    @Bean
    public SwaggerProxyGlobalFilter createSwaggerProxyGlobalFilter(){
        return new SwaggerProxyGlobalFilter();
    }
}
