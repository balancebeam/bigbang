package io.anyway.bigbang.gateway.configuration;

import io.anyway.bigbang.gateway.property.GatewayProperties;
import io.anyway.bigbang.gateway.service.DynamicRouteService;
import io.anyway.bigbang.gateway.service.IpBlackListService;
import io.anyway.bigbang.gateway.service.ResourceStrategyService;
import io.anyway.bigbang.gateway.service.WhiteListService;
import io.anyway.bigbang.gateway.service.impl.IpBlackListServiceImpl;
import io.anyway.bigbang.gateway.service.impl.RouteDefinitionStrategyServiceImpl;
import io.anyway.bigbang.gateway.service.impl.WhiteListStrategyServiceImpl;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayConfiguration {

    @Bean("whitelist#ResourceStrategyService")
    public WhiteListService createWhiteListService(){
        return new WhiteListStrategyServiceImpl();
    }

    @Bean("blacklist#ResourceStrategyService")
    public IpBlackListService createIpBlackListService(){
        return new IpBlackListServiceImpl();
    }

    @Bean
    public DynamicRouteService createDynamicRouteService(){
        return new DynamicRouteService();
    }

    @Bean("route#ResourceStrategyService")
    public ResourceStrategyService createRouteDefinitionStrategyService(){
        return new RouteDefinitionStrategyServiceImpl();
    }
}

