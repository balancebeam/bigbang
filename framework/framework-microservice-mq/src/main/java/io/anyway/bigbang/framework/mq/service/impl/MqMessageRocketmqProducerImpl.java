package io.anyway.bigbang.framework.mq.service.impl;

import io.anyway.bigbang.framework.discovery.DiscoveryMetadataService;
import io.anyway.bigbang.framework.mq.config.MqClientDescriptor;
import io.anyway.bigbang.framework.mq.config.MqClientProperties;
import io.anyway.bigbang.framework.mq.constant.MessageStateEnum;
import io.anyway.bigbang.framework.mq.constant.MqClientConstants;
import io.anyway.bigbang.framework.mq.constant.MqTypeEnum;
import io.anyway.bigbang.framework.mq.domain.MessageProducerInbound;
import io.anyway.bigbang.framework.mq.domain.MessageProducerOutbound;
import io.anyway.bigbang.framework.mq.service.MqMessageProducer;
import io.anyway.bigbang.framework.utils.JsonUtil;
import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;

import static org.apache.rocketmq.client.producer.SendStatus.SEND_OK;

@Slf4j
public class MqMessageRocketmqProducerImpl implements MqMessageProducer {

    private DefaultMQProducer mqMessageProducer;

    @Resource
    private MqClientProperties mqClientProperties;

    @Resource
    private ApplicationContext ctx;

    private MessageQueueSelector messageQueueSelector;

    @Resource
    private DiscoveryMetadataService discoveryMetadataService;

    @PostConstruct
    private void init() {

        MqClientDescriptor descriptor = mqClientProperties.getClient().get(MqTypeEnum.ROCKETMQ.getCode());
        Map<String,Object> properties= descriptor.getProducer();
        mqMessageProducer = new DefaultMQProducer((String)properties.getOrDefault("group",discoveryMetadataService.getServiceId()));
        mqMessageProducer.setRetryTimesWhenSendFailed((Integer) properties.getOrDefault("retry-times-when-send-failed",2));
        mqMessageProducer.setRetryTimesWhenSendAsyncFailed((Integer) properties.getOrDefault("retry-times-when-send-async-failed",2));
        mqMessageProducer.setSendMsgTimeout((Integer) properties.getOrDefault("send-message-timeout",3000));
        mqMessageProducer.setMaxMessageSize((Integer) properties.getOrDefault("max-message-size",4194304));
        mqMessageProducer.setCompressMsgBodyOverHowmuch((Integer) properties.getOrDefault("compress-message-body-threshold",4096));
        mqMessageProducer.setRetryAnotherBrokerWhenNotStoreOK((Boolean) properties.getOrDefault("retry-next-server",false));
        mqMessageProducer.setNamesrvAddr(descriptor.getServerAddress());

        try {
            if (properties.containsKey("messageQueueSelector")) {
                Class<? extends MessageQueueSelector> aClass = (Class<? extends MessageQueueSelector>)getClass().getClassLoader().loadClass((String) properties.get("messageQueueSelector"));
                try {
                    messageQueueSelector = ctx.getBean(aClass);
                }catch (BeansException ex){
                    messageQueueSelector= aClass.newInstance();
                }
            }
        }catch (Exception e){
            log.error("init messageQueueSelector error: {}",e.getMessage());
        }

        try {
            mqMessageProducer.start();
        } catch (Throwable t) {
            log.error("Fail to start the mqMessageProducer");
            throw new RuntimeException(t);
        }
    }

    @Override
    public MessageProducerOutbound send(MessageProducerInbound inbound) throws Exception{
        Message message = new Message(
                inbound.getDestination(),
                inbound.getTags(),
                inbound.getMessageBody().getBytes(RemotingHelper.DEFAULT_CHARSET));
        Map<String,Object> attribute= Collections.EMPTY_MAP;
        if(!StringUtils.isEmpty(inbound.getAttribute())){
            attribute= JsonUtil.fromString2Object(inbound.getAttribute(),Map.class);
        }

        if ((Boolean)attribute.getOrDefault("idempotentOn",false)) {
            String messageKey= Joiner.on(MqClientConstants.MESSAGE_KEY_SPLITTER).join(
                    inbound.getDestination(),
                    StringUtils.isNotBlank(inbound.getTags()) ? inbound.getTags() : "",
                    attribute.getOrDefault("messageKey",""));
            message.setKeys(messageKey);
        }
        // put the message header
        if (StringUtils.isNotBlank(inbound.getMessageHeader())) {
            message.putUserProperty("MQMessageHeader", inbound.getMessageHeader());
        }

        SendResult result;
        if(messageQueueSelector== null){
            result = mqMessageProducer.send(message);
        }
        else{
            result = mqMessageProducer.send(message, messageQueueSelector, attribute.getOrDefault("sendOpts",""));
        }
        log.info("Send message result, id={}, status={}", result.getMsgId(), result.getSendStatus());
        MessageProducerOutbound messageProducerResponse = new MessageProducerOutbound();
        messageProducerResponse.setMessageId(result.getMsgId());
        if(result.getSendStatus()==SEND_OK) {
            messageProducerResponse.setState(MessageStateEnum.SUCCESS);
        }
        else{
            messageProducerResponse.setState(MessageStateEnum.FAILURE);
            messageProducerResponse.setCause(result.getSendStatus().name());
        }
        return messageProducerResponse;
    }

    @Override
    public MqTypeEnum type() {
        return MqTypeEnum.ROCKETMQ;
    }

}
