package io.anyway.bigbang.framework.mq.controller;


import io.anyway.bigbang.framework.model.api.ApiResponseEntity;
import io.anyway.bigbang.framework.model.enumeration.EnumStatement;
import io.anyway.bigbang.framework.mq.constant.MqTypeEnum;
import io.anyway.bigbang.framework.mq.domain.MessageListenerDefinition;
import io.anyway.bigbang.framework.mq.domain.MessageListenerInbound;
import io.anyway.bigbang.framework.mq.entity.MqClientIdempotentEntity;
import io.anyway.bigbang.framework.mq.service.MqConsumerClientManager;
import io.anyway.bigbang.framework.mq.service.MqMessageListenerDispatcher;
import io.anyway.bigbang.framework.mq.service.impl.MqMessageListenerDispatcherImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RequestMapping("/mq/consumer")
public class MqConsumerController {

    @Resource
    private MqMessageListenerDispatcher mqMessageListenerDispatcher;

    @Resource
    private MqConsumerClientManager mqConsumerClientManager;

    @RequestMapping(value = "/route/forward", method = RequestMethod.POST)
    public String doMessageRouteForward(@RequestHeader("MQ_TYPE_CODE") String mqTypeCode,
                                        @RequestHeader("CONSUMER_DEFINITION_ID") String id,
                                        @RequestBody MessageListenerInbound inbound) {
        log.debug("Start the routing the message request: {}, mqTypeCode: {}, CONSUMER_DEFINITION_ID: {}", inbound,mqTypeCode,id);
        MqTypeEnum mqTypeEnum= EnumStatement.of(MqTypeEnum.class,mqTypeCode);
        MessageListenerDefinition messageListenerDefinition = ((MqMessageListenerDispatcherImpl)mqMessageListenerDispatcher).findMessageListenerDefinition(mqTypeEnum,id);
        if(messageListenerDefinition == null) {
            throw new RuntimeException("Cannot find MessageConsumerDefinition, id="+id);
        }
        mqMessageListenerDispatcher.onMessage(inbound, messageListenerDefinition);
        log.info("Handle the message successfully");
        return "OK";
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public ApiResponseEntity<List<MqClientIdempotentEntity>> query() {
        List<MqClientIdempotentEntity> list=  mqConsumerClientManager.queryAll();
        return ApiResponseEntity.ok(list);
    }

    @RequestMapping(value = "/purge", method = RequestMethod.GET)
    public void purge() {
        mqConsumerClientManager.purge();
    }

}
