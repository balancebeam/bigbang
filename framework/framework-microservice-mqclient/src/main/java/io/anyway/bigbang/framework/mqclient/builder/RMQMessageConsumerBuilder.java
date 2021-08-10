package io.anyway.bigbang.framework.mqclient.builder;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.mqclient.domain.MessageHeader;
import io.anyway.bigbang.framework.mqclient.domain.MessageType;
import io.anyway.bigbang.framework.mqclient.domain.MqClientConfig;
import io.anyway.bigbang.framework.mqclient.domain.MqClientMessage;
import io.anyway.bigbang.framework.mqclient.metrics.MqClientMetric;
import io.anyway.bigbang.framework.mqclient.service.impl.MqMessageConsumer;
import io.anyway.bigbang.framework.mqclient.utils.MqUtils;
import io.anyway.bigbang.framework.mqclient.service.impl.MqMessageListenerWrapper;
import io.anyway.bigbang.framework.mqclient.utils.MqClientConstants;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.*;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.body.ConsumerRunningInfo;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.context.SmartLifecycle;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;


@Slf4j
public class RMQMessageConsumerBuilder implements MqMessageConsumerBuilder, SmartLifecycle {
    @Resource
    private MqMessageListenerWrapper mqMessageListenerWrapper;

    private List<DefaultMQPushConsumer> consumerList = Lists.newArrayList();

    private boolean isRunning = false;

    private static final AtomicLong msgReceivedCount = new AtomicLong(0);

    private static final AtomicLong msgReceivedFailureCount = new AtomicLong(0);

    private void handleMessageExt(List<MessageExt> list, MqClientConfig mqClientConfig) throws Exception {
        for (MessageExt messageExt : list) {
            String headers = messageExt.getUserProperty(MqUtils.MQ_MESSAGE_HEADER);
            log.info("from MessageHeader={}", headers);
            MessageHeader messageHeader = null;
            if (StringUtils.isNotBlank(headers)) {
                messageHeader = JSONObject.parseObject(headers, MessageHeader.class);
            }
            try {
                String messageBody = new String(messageExt.getBody(), RemotingHelper.DEFAULT_CHARSET);
                log.debug("RMQ consumer, tags={}, consumeTimes={}, msgId={}, msgBody={}, keys={}",
                        messageExt.getTags(), messageExt.getReconsumeTimes(),
                        messageExt.getMsgId(), messageBody, messageExt.getKeys());
                MqClientMessage mqClientMessage = new MqClientMessage();
                mqClientMessage.setMessage(messageBody);
                mqClientMessage.setTags(messageExt.getTags());
                mqClientMessage.setDestination(messageExt.getTopic());
                mqClientMessage.setMessageType(MessageType.TOPIC.name());
                mqClientMessage.setMessageId(messageExt.getMsgId());
                mqClientMessage.setMessageHeader(messageHeader);
                if (StringUtils.isNotBlank(messageExt.getKeys()) &&
                        messageExt.getKeys().contains(MqClientConstants.MESSAGE_KEY_SPLITTER)) {
                    mqClientMessage.setMessageKey(messageExt.getKeys());
                } else {
                    mqClientMessage.setMessageKey(messageExt.getMsgId());
                }
                msgReceivedCount.incrementAndGet();
                mqMessageListenerWrapper.process(mqClientConfig, mqClientMessage);

            } catch (Exception e) {
                msgReceivedFailureCount.incrementAndGet();
                log.warn("Exception happened", e);
                throw e;
            }
        }
    }

    @Override
    public List<MqMessageConsumer> build(MqClientConfig mqClientConfig) {
        log.debug("RMQMessageConsumerBuilder, build the RMQ consumer, param={}", mqClientConfig);

        List<MqMessageConsumer> mqMessageConsumerList = Lists.newArrayList();
        for (Map.Entry<String, String> consumerConfig : mqClientConfig.getConsumerGroupTopicConf().entrySet()) {
            DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerConfig.getKey());
            consumer.setNamesrvAddr(mqClientConfig.getNameSrv());
            try {
                List<String> tags = mqClientConfig.getListenedTagsConf().get(consumerConfig.getValue());
                if (tags == null || tags.isEmpty()) {
                    consumer.subscribe(consumerConfig.getValue(), "*");
                } else {
                    consumer.subscribe(consumerConfig.getValue(), Joiner.on("||").join(tags));
                }
                consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
                consumer.setInstanceName(UUID.randomUUID().toString());
                if (mqClientConfig.isMsgListeningOrderly()) {
                    consumer.registerMessageListener(new MessageListenerOrderly() {

                        @Override
                        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> messages, ConsumeOrderlyContext context) {
                            try {
                                handleMessageExt(messages, mqClientConfig);
                            } catch (Exception e) {
                                log.error("Exception happened when handling message ", e);
                                return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                            }
                            return ConsumeOrderlyStatus.SUCCESS;
                        }
                    });
                } else {
                    consumer.registerMessageListener((MessageListenerConcurrently) (messages, context) -> {
                        try {
                            handleMessageExt(messages, mqClientConfig);
                        } catch (Exception e) {
                            log.error("Exception happened when handling message ", e);
                            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                        }
                        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                    });
                }

                consumerList.add(consumer);
            } catch (Exception e) {
                log.error("Exception happened when starting RMQConsumer", e);
            }
            MqMessageConsumer mqMessageConsumer = new MqMessageConsumer();
            mqMessageConsumer.setDefaultMQPushConsumer(consumer);
            mqMessageConsumerList.add(mqMessageConsumer);
        }

        return mqMessageConsumerList;
    }

    @Override
    public void start() {
        log.info("Start the mq consumer list.");
        try {
            for (DefaultMQPushConsumer consumer : consumerList) {
                consumer.start();
            }
        } catch (Exception e) {
            log.error("Fail to start the mq consumer", e);
            throw new RuntimeException(e);
        }
        log.info("Successfully started the mq consumer list.");

        isRunning = true;
    }

    @Override
    public int getPhase() {
        return 0;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }


    @Override
    public boolean isRunning() {
        return isRunning;
    }


    @Override
    public void stop(Runnable callback) {
        log.info("Stop the mq consumer list.");

        callback.run();

        try {
            for (DefaultMQPushConsumer consumer : consumerList) {
                consumer.shutdown();
            }
        } catch (Exception e) {
            log.error("Fail to stop the mq consumer", e);
            throw new RuntimeException(e);
        }

        isRunning = false;
    }

    @Override
    public void stop() {
        log.info("Stopping...");
        isRunning = false;
    }

    public List<MqClientMetric> getMqClientMetrics() {
        List<MqClientMetric> mqClientMetrics = Lists.newArrayList();
        for (DefaultMQPushConsumer consumer : consumerList) {
            MqClientMetric metric = new MqClientMetric();

            ConsumerRunningInfo runningInfo = consumer.getDefaultMQPushConsumerImpl().consumerRunningInfo();
            Properties properties = runningInfo.getProperties();

            metric.setConsumerGroupName(consumer.getConsumerGroup() + "-" + consumer.getInstanceName());
            metric.setConsumerThreadCoreSize(
                    properties.getProperty(ConsumerRunningInfo.PROP_THREADPOOL_CORE_SIZE) != null ?
                            Integer.valueOf(properties.getProperty(ConsumerRunningInfo.PROP_THREADPOOL_CORE_SIZE)) : -1);
            metric.setConsumerStatusInfo(runningInfo.getMqTable());
            //consumer.getMessageListener().
            mqClientMetrics.add(metric);

        }
        MqClientMetric overallMetric = new MqClientMetric();
        overallMetric.setAllFailureReceivedMsgSize(msgReceivedFailureCount.get());
        overallMetric.setAllReceivedMsgSize(msgReceivedCount.get());
        mqClientMetrics.add(overallMetric);
        return mqClientMetrics;
    }
}
