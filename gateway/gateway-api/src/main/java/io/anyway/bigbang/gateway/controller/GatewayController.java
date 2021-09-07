package io.anyway.bigbang.gateway.controller;

import io.anyway.bigbang.gateway.service.DynamicRouteService;
import io.anyway.bigbang.framework.model.api.ApiResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/")
public class GatewayController {

    @Resource
    private DynamicRouteService dynamicRouteService;

    @ResponseBody
    @GetMapping("/router")
    public ApiResponseEntity<List<Map<String, Object>>> getRouter() throws Exception{
        List<Map<String, Object>> list= dynamicRouteService.getRoutesList();
        log.info("route list: {}",list);
        return ApiResponseEntity.ok(list);
    }

}
