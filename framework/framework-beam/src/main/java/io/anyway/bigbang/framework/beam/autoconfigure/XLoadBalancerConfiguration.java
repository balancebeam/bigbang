package io.anyway.bigbang.framework.beam.autoconfigure;

import io.anyway.bigbang.framework.beam.controller.RouterStrategyController;
import io.anyway.bigbang.framework.beam.loadbalancer.LoadBalancerProcessor;
import io.anyway.bigbang.framework.beam.service.*;
import io.anyway.bigbang.framework.beam.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;


@Slf4j
@RibbonClients(defaultConfiguration = XRibbonClientsConfiguration.class)
public class XLoadBalancerConfiguration {

    @Bean
    public LoadBalancerProcessor createLoadBalancerProcessor(){
        return new LoadBalancerProcessor();
    }

    @Bean
    @ConditionalOnMissingBean
    public NodeMetadataService createEurekaMetadataService(){
        return new EurekaMetadataServiceImpl();
    }

    @Bean("offline#RouterStrategyCellular")
    public RouterStrategyCellular createOfflineRouterStrategyCellular(){
        return new OfflineRouterStrategyCellularImpl();
    }

    @Order(100001)
    @Bean("unit#RouterStrategyCellular")
    public RouterStrategyCellular createUnitRouterPredicateCellular(){
        return new UnitRouterPredicateCellularImpl();
    }

    @Order(100002)
    @Bean("user#RouterStrategyCellular")
    public RouterStrategyCellular createUserRouterPredicateCellular(){
        return new UserRouterPredicateCellularImpl();
    }

    @Bean("weight#RouterStrategyCellular")
    public RouterStrategyCellular createWeightRouterPredicateCellular(){
        return new WeightRouterStrategyCellularImpl();
    }

    @Order(100003)
    @Bean("versionRoutePredict")
    public NodeRouterPredicate createVersionRouterPredicate(){
        return new VersionRouterPredicateImpl();
    }

    @Bean
    public RouterStrategyService createRouterStrategyService(){
        return new RouterStrategyService();
    }

    @Bean
    public RouterStrategyController createRouterStrategyController(){
        return new RouterStrategyController();
    }

}
