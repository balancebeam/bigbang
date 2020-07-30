package io.anyway.bigbang.framework.core.logging.controller;

import io.anyway.bigbang.framework.core.logging.LoggingLevelExchange;
import io.anyway.bigbang.framework.core.rest.RestHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/logging/management")
public class LogManagementController {


    @RequestMapping(value = "/exchange", method = RequestMethod.PUT)
    public RestHeader exchange(@RequestBody @Valid LogExchangeRequest request){
        boolean result= LoggingLevelExchange.exchange(request.getName(),request.getLevel());
        RestHeader response= new RestHeader();
        response.setMsg("exchange result: "+ result);
        return response;
    }

}
