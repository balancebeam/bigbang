package io.anyway.bigbang.framework.mq.controller;


import io.anyway.bigbang.framework.mq.service.MqProducerClientManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("/mq/producer")
public class MqProducerController {

    @Resource
    private MqProducerClientManager mqProducerClientManager;

    @RequestMapping(value = "/failover}", method = RequestMethod.GET)
    public void failover() {
        mqProducerClientManager.failover();
    }

    @RequestMapping(value = "/purge", method = RequestMethod.GET)
    public void purge() {
        mqProducerClientManager.purge();
    }

}
