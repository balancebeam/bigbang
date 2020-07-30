package io.anyway.bigbang.devops.deployment.controller;


import io.anyway.bigbang.framework.beam.domain.*;
import io.anyway.bigbang.framework.beam.service.RouterStrategyService;
import io.anyway.bigbang.framework.beam.service.UnitStrategyQuerier;
import io.anyway.bigbang.framework.beam.service.UserStrategyQuerier;
import io.anyway.bigbang.framework.core.rest.RestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/beam/management/strategy")
public class BeamStrategyController {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private UnitStrategyQuerier unitStrategyQuerier;

    @Resource
    private UserStrategyQuerier userStrategyQuerier;

    @Resource
    private RouterStrategyService routerStrategyService;

    @RequestMapping(value = "/unit", method = RequestMethod.POST)
    public String setUnitStrategies(@RequestBody UnitRouterStrategyWrapper strategies) {
        log.info("UnitRouterStrategyWrapper: {}",strategies);
        routerStrategyService.setUnitStrategies(strategies);
        return "ok";
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String setUserStrategies(@RequestBody UserRouterStrategyWrapper strategies) {
        log.info("UserRouterStrategyWrapper: {}",strategies);
        routerStrategyService.setUserStrategies(strategies);
        return "ok";
    }

    @RequestMapping(value = "/weight", method = RequestMethod.POST)
    public String setWeightStrategies(@RequestBody WeightRouterStrategy strategy) {
        log.info("WeightRouterStrategy: {}",strategy);
        String path= "http://"+ strategy.getHostPort()+"/beam/management/weight/"+ strategy.getWeight();
        log.debug("Modify weight path: {}",path);
        ResponseEntity<String> responseEntity= restTemplate.getForEntity(path,String.class);
        return responseEntity.getBody();
    }

    @RequestMapping(value = "/unit", method = RequestMethod.GET)
    public RestBody<Collection<UnitRouterStrategy>> queryUnitRouterStrategies(){
        RestBody<Collection<UnitRouterStrategy>> restBody= new RestBody<>();
        restBody.setData(unitStrategyQuerier.queryAll());
        return restBody;
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public RestBody<Collection<UserRouterStrategy>> queryUserRouterStrategies(){
        RestBody<Collection<UserRouterStrategy>> restBody= new RestBody<>();
        restBody.setData(userStrategyQuerier.queryAll());
        return restBody;
    }
}
