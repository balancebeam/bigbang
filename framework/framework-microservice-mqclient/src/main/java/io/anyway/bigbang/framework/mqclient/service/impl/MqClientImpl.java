package io.anyway.bigbang.framework.mqclient.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.ttl.TtlRunnable;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import io.anyway.bigbang.framework.discovery.DiscoveryMetadataService;
import io.anyway.bigbang.framework.gray.GrayContext;
import io.anyway.bigbang.framework.gray.GrayContextHolder;
import io.anyway.bigbang.framework.mqclient.core.AfterCommitTaskRegister;
import io.anyway.bigbang.framework.mqclient.dao.MqClientMessageRepository;
import io.anyway.bigbang.framework.mqclient.domain.*;
import io.anyway.bigbang.framework.mqclient.entity.MqClientMessageEntity;
import io.anyway.bigbang.framework.mqclient.metrics.MqClientMetric;
import io.anyway.bigbang.framework.mqclient.service.MqClient;
import io.anyway.bigbang.framework.mqclient.utils.MqClientConstants;
import io.anyway.bigbang.framework.mqclient.utils.MqUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


@Data
@Slf4j
public class MqClientImpl implements MqClient {

    @Resource
    private MqClientConfig mqClientConfig;

    @Resource
    private MqMessageProducer mqMessageProducer;

    @Resource
    private MqClientMessageRepository mqClientMessageRepository;

    @Resource
    private DiscoveryMetadataService discoveryMetadataService;

    private volatile ExecutorService workQueueExecutor;

    private Map<String, MqMessageProducer> mqMessageProducerMap = Maps.newHashMap();

    private final static AtomicLong msgSendCount = new AtomicLong(0);

    @PostConstruct
    public void init() {
        mqMessageProducerMap.put(MqType.RMQ.name(), mqMessageProducer);
    }

    public MqClientMetric getMqClientMetric() {
        if (workQueueExecutor != null) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) workQueueExecutor;

            MqClientMetric metric = new MqClientMetric();
            metric.setProducerSendingPoolActiveSize(executor.getActiveCount());
            metric.setProducerSendingPoolCoreSize(executor.getCorePoolSize());
            metric.setProducerSendingPoolMaxSize(executor.getMaximumPoolSize());
            metric.setProducerSendingPoolLargestSize(executor.getLargestPoolSize());
            metric.setProducerQueuedTask(executor.getQueue().size());
            metric.setAllSentMsgSize(msgSendCount.get());
            return metric;
        }

        return null;
    }

    private ExecutorService getWorkQueueExecutor() {
        if (workQueueExecutor != null) {
            return workQueueExecutor;
        }
        synchronized (this) {
            if (workQueueExecutor == null) {
                workQueueExecutor = new ThreadPoolExecutor(
                        mqClientConfig.getWorkQueueCorePoolSize(),
                        mqClientConfig.getWorkQueueMaxPoolSize(),
                        mqClientConfig.getWorkQueueKeepAliveSeconds(), TimeUnit.SECONDS,
                        new LinkedBlockingDeque<>(mqClientConfig.getWorkQueueCapacity()),
                        new ThreadFactory() {
                            ThreadGroup threadGroup = new ThreadGroup("MqThreadGroup");
                            AtomicInteger threadIndex = new AtomicInteger(0);

                            @Override
                            public Thread newThread(Runnable r) {
                                int idx = threadIndex.incrementAndGet();
                                Thread thread = new Thread(threadGroup, r, "mq-client-" + idx);
                                return thread;
                            }
                        },
                        new ThreadPoolExecutor.AbortPolicy()
                );
            }
            log.info("WorkQueueExecutor init is done.");
            return workQueueExecutor;
        }
    }

    public void doSend(MqClientMessageContext mqClientMessageContext) {
        // send message
        log.info("Start to send mq with context={}", mqClientMessageContext);

        msgSendCount.incrementAndGet();

        MqClientMessageEntity messageEntity = mqClientMessageContext.getMqClientMessageEntity();
        Map<String, Object> params = Maps.newHashMap();
        params.put("id", messageEntity.getId());
        params.put("partitionKey", messageEntity.getPartitionKey());
        try {
            MqMessageProducer mqMessageProducer = mqMessageProducerMap.get(messageEntity.getMqType());
            if (mqMessageProducer == null) {
                log.error("Fail to get message producer for mqType={}", messageEntity.getMqType());
                return;
            }
            SendResult result = mqMessageProducer.send(messageEntity);
            if (SendStatus.SEND_OK.equals(result.getSendStatus())) {
                if (mqClientMessageContext.getPersistMode() == MessagePersistModeStatus.BURN_AFTER_SENT) {
                    log.info("burn message params={}", params);
                    mqClientMessageRepository.purgeMessageById(messageEntity.getId());
                    return;
                }
                params.put("status", MessageClientStatus.SUCCESS.name());
                params.put("messageId", result.getMsgId());
            } else {
                params.put("status", MessageClientStatus.FAILED.name());
                params.put("nextRetryAt", new Date(new Date().getTime() + MqClientConstants.NEXT_RETRY_GAP));
            }
            log.info("doSend params={}", params);
        } catch (Throwable t) {
            log.error("Fail to send the message, params={}", params, t);

            params.put("status", MessageClientStatus.FAILED.name());
            params.put("nextRetryAt", new Date(new Date().getTime() + MqClientConstants.NEXT_RETRY_GAP));
        }

        if (mqClientMessageContext.getPersistMode() != MessagePersistModeStatus.BURN_BEFORE_SEND) {
            log.info("Updating the message status with param={}", params);
            if (mqClientMessageContext.isFailoverCtx()) {
                params.put("retryCount", messageEntity.getRetryCount() + 1);
            }
            mqClientMessageRepository.updateMessageStatus(params);
        }
    }

    public void send(MqSendOption mqSendOption) {
        log.info("MqClientServiceImpl.send, param={}", mqSendOption);

        MqClientMessageEntity messageEntity = new MqClientMessageEntity();
        messageEntity.setBizId(mqSendOption.getBizId());
        messageEntity.setBizType(mqSendOption.getBizType());
        messageEntity.setMessage(mqSendOption.getMessage());
        messageEntity.setMessageType(mqSendOption.getMessageType().name());
        messageEntity.setDestination(mqSendOption.getDestination());
        messageEntity.setStatus(MessageClientStatus.NEW.name());
        messageEntity.setTags(mqSendOption.getTags());
        messageEntity.setNextRetryAt(new Date(new Date().getTime() + MqClientConstants.NEXT_RETRY_GAP));
        messageEntity.setPartitionKey(MqUtils.evalPartitionKey(new Date()));
        messageEntity.setMqType(mqClientConfig.getMqType().name());
        messageEntity.setSendOpts(mqSendOption.getSendOpts());
        messageEntity.setMessageHeader(JSONObject.toJSONString(buildMessageHeader(mqSendOption)));
        messageEntity.setPersistMode(mqSendOption.getPersistMode().getVal());

        if (mqSendOption.isIdempotentOn()) {
            messageEntity.setMessageKey(Joiner.on(MqClientConstants.MESSAGE_KEY_SPLITTER).join(
                    mqSendOption.getDestination(),
                    StringUtils.isNotBlank(mqSendOption.getTags()) ? mqSendOption.getTags() : "",
                    StringUtils.isNotBlank(mqSendOption.getMessageKey()) ? mqSendOption.getMessageKey() : "")
            );
        }

        MqClientMessageContext mqClientMessageContext = new MqClientMessageContext();
        mqClientMessageContext.setMqClientMessageEntity(messageEntity);
        mqClientMessageContext.setFailoverCtx(mqSendOption.isFailOverContext());
        mqClientMessageContext.setPersistMode(mqSendOption.getPersistMode());

        if (mqSendOption.getPersistMode() != MessagePersistModeStatus.BURN_BEFORE_SEND) {
            mqClientMessageRepository.insertMessage(messageEntity);
            log.info("insert mq client record successfully,id={}", messageEntity.getId());
        }

        final ExecutorService workQueueExecutor = getWorkQueueExecutor();

        AfterCommitTaskRegister.registerTask(
                () -> workQueueExecutor.execute(
                        TtlRunnable.get(() -> doSend(mqClientMessageContext))
                )
        );
    }

    @PreDestroy
    public void destroy() {
        if (workQueueExecutor != null) {
            List<Runnable> abortedElements = workQueueExecutor.shutdownNow();
            if (abortedElements != null && !abortedElements.isEmpty()) {
                log.error("workQueue aborted elements={}", abortedElements);
            }
        }
    }

    private MessageHeader buildMessageHeader(MqSendOption mqSendOption) {
        MessageHeader messageHeader = new MessageHeader();
        Optional<GrayContext> optional= GrayContextHolder.getGrayContext();
        if(optional.isPresent()) {
            messageHeader.setGrayContext(optional.get());
        }
        messageHeader.setServiceId(discoveryMetadataService.getServiceId());
        messageHeader.setIp(discoveryMetadataService.getIp());
        messageHeader.setPersistMode(mqSendOption.getPersistMode().getVal());
        String version = discoveryMetadataService.getVersion();
        if (!StringUtils.isEmpty(version)) {
            messageHeader.setVersion(version);
        }
        log.info("in sending, MessageHeader={}", messageHeader);
        return messageHeader;
    }

}
