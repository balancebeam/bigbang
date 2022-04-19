package io.anyway.bigbang.framework.mq.service.impl;

import io.anyway.bigbang.framework.discovery.DiscoveryMetadataService;
import io.anyway.bigbang.framework.mq.config.MqClientDescriptor;
import io.anyway.bigbang.framework.mq.config.MqClientProperties;
import io.anyway.bigbang.framework.mq.constant.MqTypeEnum;
import io.anyway.bigbang.framework.mq.domain.MessageListenerDefinition;
import io.anyway.bigbang.framework.mq.domain.MessageListenerInbound;
import io.anyway.bigbang.framework.mq.service.MqMessageListenerDispatcher;
import io.anyway.bigbang.framework.mq.service.MqMessageListener;
import io.anyway.bigbang.framework.mq.domain.MessageHeader;
import io.anyway.bigbang.framework.utils.JsonUtil;
import io.anyway.bigbang.framework.utils.MD5Util;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class MqMessageRocketmqListenerImpl implements MqMessageListener {

    final private List<DefaultMQPushConsumer> consumerList = Lists.newArrayList();

    @Resource
    private MqMessageListenerDispatcher mqMessageDispatcherListener;

    @Resource
    private MqClientProperties mqClientProperties;

    @Resource
    private DiscoveryMetadataService discoveryMetadataService;

    @Override
    public void start(){
        Collection<MessageListenerDefinition> list= mqMessageDispatcherListener.getMessageListenerDefinitionList(MqTypeEnum.ROCKETMQ);
        MqClientDescriptor descriptor= mqClientProperties.getClient().get(MqTypeEnum.ROCKETMQ.getCode());
        Map<String,Object> properties= descriptor.getConsumer();
        for(MessageListenerDefinition each: list) {
            String group= !StringUtils.isEmpty(each.getGroup())? each.getGroup() :  discoveryMetadataService.getServiceId()+ "_"+ MD5Util.getMD5String(each.getDestination()+"_"+ each.getTags());
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(group);
            int heartbeatBrokerInterval= (Integer)properties.getOrDefault("heartbeatBrokerInterval",15000);
            consumer.setHeartbeatBrokerInterval(heartbeatBrokerInterval);
            try {
                List<String> tags = each.getTags();
                if (tags.isEmpty()) {
                    consumer.subscribe(each.getDestination(), "*");
                } else {
                    consumer.subscribe(each.getDestination(), Joiner.on("||").join(tags));
                }
                consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
                consumer.setInstanceName(UUID.randomUUID().toString());

                if (mqClientProperties.isMsgListeningOrderly()) {
                    consumer.registerMessageListener((MessageListenerOrderly) (messages, context) -> {
                        try {
                            handleMessageExt(messages,each);
                        } catch (Exception e) {
                            log.error("Exception happened when handling message ", e);
                            return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                        }
                        return ConsumeOrderlyStatus.SUCCESS;
                    });
                } else {
                    consumer.registerMessageListener((MessageListenerConcurrently) (messages, context) -> {
                        try {
                            handleMessageExt(messages,each);
                        } catch (Exception e) {
                            log.error("Exception happened when handling message ", e);
                            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                        }
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    });
                }
                consumerList.add(consumer);
            }catch (Exception e) {
                log.error("Exception happened when starting RMQConsumer", e);
            }
        }
        try {
            for (DefaultMQPushConsumer consumer : consumerList) {
                consumer.start();
            }
        } catch (Exception e) {
            log.error("Fail to start the mq consumer", e);
            throw new RuntimeException(e);
        }
    }

    private void handleMessageExt(List<MessageExt> list, MessageListenerDefinition messageListenerDefinition) throws Exception {
        for (MessageExt messageExt : list) {
            String headers = messageExt.getUserProperty("MQMessageHeader");
            log.info("RocketMQ receive the MessageHeader: {}", headers);
            MessageHeader messageHeader = null;
            if (!StringUtils.isEmpty(headers)) {
                messageHeader = JsonUtil.fromString2Object(headers,MessageHeader.class);
            }
            try {
                String messageBody = new String(messageExt.getBody(), RemotingHelper.DEFAULT_CHARSET);
                log.debug("RMQ consumer, tags={}, consumeTimes={}, msgId={}, msgBody={}, keys={}",
                        messageExt.getTags(), messageExt.getReconsumeTimes(),
                        messageExt.getMsgId(), messageBody, messageExt.getKeys());

                MessageListenerInbound messageListenerInbound= new MessageListenerInbound();
                messageListenerInbound.setMessageHeader(messageHeader)
                    .setMqType(MqTypeEnum.ROCKETMQ)
                    .setMessageBody(messageBody)
                    .setDestination(messageExt.getTopic())
                    .setTags(messageExt.getTags())
                    .setMessageId(messageExt.getMsgId())
                    .getAttribute().put("messageKey",messageExt.getKeys());
                mqMessageDispatcherListener.onMessage(messageListenerInbound, messageListenerDefinition);
            } catch (Exception e) {
                log.warn("Exception happened", e);
                throw e;
            }
        }
    }


    @Override
    public void stop() {
        try {
            for (DefaultMQPushConsumer consumer : consumerList) {
                consumer.shutdown();
            }
        } catch (Exception e) {
            log.error("Fail to stop the mq consumer", e);
            throw new RuntimeException(e);
        }
    }

}
