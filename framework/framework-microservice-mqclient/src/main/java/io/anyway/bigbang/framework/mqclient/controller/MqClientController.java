package io.anyway.bigbang.framework.mqclient.controller;


import io.anyway.bigbang.framework.mqclient.domain.*;
import io.anyway.bigbang.framework.mqclient.service.impl.MqClientFailover;
import io.anyway.bigbang.framework.mqclient.service.impl.MqMessageListenerWrapper;
import io.anyway.bigbang.framework.mqclient.service.impl.MqPurgeJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("/mq")
public class MqClientController {
    @Resource
    private MqClientFailover mqClientFailover;

    @Resource
    private MqPurgeJobService mqPurgeJobService;

    @Resource
    private MqClientConfig mqClientConfig;

    @Resource
    private MqMessageListenerWrapper mqMessageListenerWrapper;

    @RequestMapping(value = "/failover/{size}", method = RequestMethod.GET)
    public Object failover(@PathVariable Integer size) {
        log.info("Start the mq failover, size={}", size);
        try {
            mqClientFailover.failover(size);
        } catch (Exception e) {
            log.error("Exception happened when executing the mq failover job", e);
        }
        RestHeader result = new RestHeader();
        return result;
    }

    @RequestMapping(value = "/purge/{monthAhead}/{jobTypeCode}", method = RequestMethod.GET)
    public Object purgeMqTableRecords(@PathVariable Integer monthAhead, @PathVariable String jobTypeCode) {
        log.info("Start the job to purge the mq records, monthAhead={}, jobType={}", monthAhead, jobTypeCode);
        try {
            PurgeJobContext purgeJobContext = new PurgeJobContext();
            purgeJobContext.setMonthAhead(monthAhead);
            purgeJobContext.setPurgeJobType(PurgeJobType.getPurgeJobTypeByCode(jobTypeCode));
            mqPurgeJobService.purge(purgeJobContext);
        } catch (Exception e) {
            log.error("Exception happened when executing the mq purge job", e);
        }
        RestHeader result = new RestHeader();
        return result;
    }

    @RequestMapping(value = "/routing", method = RequestMethod.POST)
    public Object messageRouting(@RequestBody MqClientMessage mqClientMessage) {
        log.debug("Start the routing the message request={}", mqClientMessage);
        try {
            mqMessageListenerWrapper.localProcess(mqClientConfig, mqClientMessage);
        } catch (Exception e) {
            log.error("Exception happened when executing the routing the message request", e);
        }
        RestHeader result = new RestHeader();
        log.info("Handle the message successfully");
        return result;
    }

}
