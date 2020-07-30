package io.anyway.bigbang.framework.beam.controller;

import io.anyway.bigbang.framework.beam.service.RouterStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/beam/management")
public class RouterStrategyController {

    @Resource
    private RouterStrategyService routerStrategyService;

    @Resource
    private Environment environment;

    @RequestMapping(value = "/offline", method = RequestMethod.GET)
    public String shutdown(){
        routerStrategyService.shutdown();
        return "ok";
    }

    @RequestMapping(value = "/weight/{val}", method = RequestMethod.GET)
    public String modifyWeight(@PathVariable int val){
        routerStrategyService.modifyWeight(val);
        return "ok";
    }

    @RequestMapping(value = "/metadata/{name}", method = RequestMethod.GET)
    public String getMetadata(@PathVariable String name){
        return environment.getProperty("eureka.instance.metadata-map."+name,"");
    }

}
