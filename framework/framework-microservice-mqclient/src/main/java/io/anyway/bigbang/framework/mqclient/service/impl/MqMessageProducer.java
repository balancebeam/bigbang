package io.anyway.bigbang.framework.mqclient.service.impl;


import io.anyway.bigbang.framework.mqclient.domain.MqClientConfig;
import io.anyway.bigbang.framework.mqclient.entity.MqClientMessageEntity;
import io.anyway.bigbang.framework.mqclient.utils.MqUtils;
import io.anyway.bigbang.framework.mqclient.utils.MqClientConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
public class MqMessageProducer {

    @Resource
    private MqClientConfig mqClientConfig;

    private volatile DefaultMQProducer mqMessageProducer;

    @PostConstruct
    private void init() {
        mqMessageProducer = new DefaultMQProducer(mqClientConfig.getProducerGroupName());
        mqMessageProducer.setRetryTimesWhenSendFailed(MqClientConstants.RMQ_MAX_RETRY_TIMES);
        mqMessageProducer.setNamesrvAddr(mqClientConfig.getNameSrv());

        try {
            mqMessageProducer.start();
        } catch (Throwable t) {
            log.error("Fail to start the mqMessageProducer");
            throw new RuntimeException(t);
        }
    }

    SendResult send(MqClientMessageEntity mqClientMessage) throws Exception {
        Message message = new Message(mqClientMessage.getDestination(), mqClientMessage.getTags(),
                mqClientMessage.getMessage().getBytes(RemotingHelper.DEFAULT_CHARSET));
        message.setKeys(mqClientMessage.getMessageKey());

        // put the message header
        if (StringUtils.isNotBlank(mqClientMessage.getMessageHeader())) {
            message.putUserProperty(MqUtils.MQ_MESSAGE_HEADER, mqClientMessage.getMessageHeader());
        }

        SendResult result;
        if (mqClientConfig.getCustomizedMQSelector() == null) {
            result = mqMessageProducer.send(message);
        } else {
            result = mqMessageProducer.send(message, mqClientConfig.getCustomizedMQSelector(), mqClientMessage.getSendOpts());
        }
        log.info("Send message result, id={}, status={}", result.getMsgId(), result.getSendStatus());

        return result;
    }

}
