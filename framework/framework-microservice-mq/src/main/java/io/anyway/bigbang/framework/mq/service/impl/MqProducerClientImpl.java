package io.anyway.bigbang.framework.mq.service.impl;

import com.alibaba.ttl.TtlRunnable;
import io.anyway.bigbang.framework.discovery.DiscoveryMetadataService;
import io.anyway.bigbang.framework.gray.GrayContext;
import io.anyway.bigbang.framework.gray.GrayContextHolder;
import io.anyway.bigbang.framework.header.HeaderContextHolder;
import io.anyway.bigbang.framework.mq.config.MqClientProperties;
import io.anyway.bigbang.framework.mq.constant.MessagePersistModeEnum;
import io.anyway.bigbang.framework.mq.constant.MessageStateEnum;
import io.anyway.bigbang.framework.mq.constant.MqClientConstants;
import io.anyway.bigbang.framework.mq.dao.MqProducerMessageMapper;
import io.anyway.bigbang.framework.mq.domain.MessageHeader;
import io.anyway.bigbang.framework.mq.domain.MessageProducerInbound;
import io.anyway.bigbang.framework.mq.domain.MessageProducerOutbound;
import io.anyway.bigbang.framework.mq.domain.MqSendOption;
import io.anyway.bigbang.framework.mq.entity.MqClientMessageEntity;
import io.anyway.bigbang.framework.mq.service.MqProducerClient;
import io.anyway.bigbang.framework.mq.service.MqMessageProducer;
import io.anyway.bigbang.framework.mq.utils.AfterCommitTaskRegister;
import io.anyway.bigbang.framework.utils.BeanMapUtils;
import io.anyway.bigbang.framework.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.naming.NameNotFoundException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class MqProducerClientImpl implements MqProducerClient {

    @Resource
    private DiscoveryMetadataService discoveryMetadataService;

    @Resource
    private MqProducerMessageMapper mqProducerMessageMapper;

    @Resource
    private MqClientProperties mqClientProperties;

    @Autowired(required = false)
    private List<MqMessageProducer> mqMessageProducerList= Collections.emptyList();

    final private Map<String,MqMessageProducer> mqMessageProducerMap= new HashMap<>();

    private volatile ExecutorService workQueueExecutor;

    @PostConstruct
    public void init(){
        for(MqMessageProducer each: mqMessageProducerList){
            Assert.notNull(each.type(),each+"'s type was empty.");
            mqMessageProducerMap.put(each.type().getCode(),each);
        }
        log.info("mqMessageProducerMap: {}",mqMessageProducerMap);
        mqMessageProducerList.clear();
    }

    @Override
    @Transactional(propagation= Propagation.SUPPORTS)
    public void send(MqSendOption mqSendOption) {
        log.info("mqSendOption: {}", mqSendOption);

        final MqClientMessageEntity messageEntity = new MqClientMessageEntity();
        messageEntity.setTransactionId(mqSendOption.getTransactionId());
        messageEntity.setTransactionType(mqSendOption.getTransactionType());
        if(mqSendOption.getMqType()!=null) {
            messageEntity.setMqType(mqSendOption.getMqType().getCode());
        }
        else{
            messageEntity.setMqType(mqClientProperties.getMqType().getCode());
        }
        messageEntity.setMessageType(mqSendOption.getMessageType().getCode());
        messageEntity.setDestination(mqSendOption.getDestination());

        Assert.notNull(mqSendOption.getMessage(),"message body was empty");
        messageEntity.setMessageBody(mqSendOption.getMessage() instanceof String?
                (String)mqSendOption.getMessage():
                JsonUtil.fromObject2String(mqSendOption.getMessage()));
        messageEntity.setMessageHeader(JsonUtil.fromObject2String(buildMessageHeader(mqSendOption)));
        messageEntity.setTags(mqSendOption.getTags());
        messageEntity.setAttribute(JsonUtil.fromObject2String(mqSendOption.getAttribute()));
        messageEntity.setState(MessageStateEnum.SUBMIT.getCode());
        messageEntity.setRetryCount(MqClientConstants.MAX_RETRY_TIMES);
        messageEntity.setRetryNextAt(new Date(System.currentTimeMillis() + MqClientConstants.NEXT_RETRY_GAP));
        messageEntity.setPersistMode(mqSendOption.getPersistMode().getCode());

        if (mqSendOption.getPersistMode() != MessagePersistModeEnum.BURN_BEFORE_SEND) {
            mqProducerMessageMapper.insertMessage(messageEntity);
            log.info("insert a messageEntity: {}", messageEntity);
        }

        AfterCommitTaskRegister.registerTask(
                () -> getWorkQueueExecutor().execute(
                        TtlRunnable.get(() -> doSend(messageEntity))
                )
        );

    }

    private MessageHeader buildMessageHeader(MqSendOption mqSendOption) {
        MessageHeader messageHeader = new MessageHeader();
        Optional<GrayContext> optional= GrayContextHolder.getGrayContext();
        if(optional.isPresent()) {
            messageHeader.setGrayContext(optional.get());
        }
        Optional<String> headerOpt= HeaderContextHolder.getHeaderValue("X-Trace-Id");
        if(headerOpt.isPresent()){
            messageHeader.setTraceId(headerOpt.get());
        }
        messageHeader.setTransactionId(mqSendOption.getTransactionId());
        messageHeader.setServiceId(discoveryMetadataService.getServiceId());
        messageHeader.setIp(discoveryMetadataService.getIp());
        messageHeader.setPersistMode(mqSendOption.getPersistMode().getCode());
        String version = discoveryMetadataService.getVersion();
        if (!StringUtils.isEmpty(version)) {
            messageHeader.setVersion(version);
        }
        log.info("messageHeader: {}", messageHeader);
        return messageHeader;
    }

    private ExecutorService getWorkQueueExecutor() {
        if (workQueueExecutor != null) {
            return workQueueExecutor;
        }
        synchronized (this) {
            if (workQueueExecutor == null) {
                workQueueExecutor = new ThreadPoolExecutor(
                        mqClientProperties.getWorkQueueCorePoolSize(),
                        mqClientProperties.getWorkQueueMaxPoolSize(),
                        mqClientProperties.getWorkQueueKeepAliveSeconds(), TimeUnit.SECONDS,
                        new LinkedBlockingDeque<>(mqClientProperties.getWorkQueueCapacity()),
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

    @PreDestroy
    public void destroy() {
        if (workQueueExecutor != null) {
            List<Runnable> abortedElements = workQueueExecutor.shutdownNow();
            if (abortedElements != null && !abortedElements.isEmpty()) {
                log.error("workQueue aborted elements={}", abortedElements);
            }
        }
    }

    public void doSend(MqClientMessageEntity messageEntity) {
        MessageProducerInbound messageProducerInbound = BeanMapUtils.map(messageEntity, MessageProducerInbound.class);
        Map<String, Object> params = new LinkedHashMap<>();
        try {
            MqMessageProducer mqMessageProducer= mqMessageProducerMap.get(messageEntity.getMqType());
            if(mqMessageProducer== null){
                throw new NameNotFoundException("Didn't find type of "+messageEntity.getMqType()+" mq producer");
            }
            MessageProducerOutbound result = mqMessageProducer.send(messageProducerInbound);
            if (MessageStateEnum.SUCCESS==result.getState()) {
                if (MessagePersistModeEnum.BURN_AFTER_SENT.getCode().equals(messageEntity.getPersistMode())) {
                    mqProducerMessageMapper.purgeMessageById(messageEntity.getId());
                    log.info("Sent message Successfully and purged it immediately, id: {}",messageEntity.getId());
                    return;
                }
                params.put("state", MessageStateEnum.SUCCESS.getCode());
                params.put("messageId", result.getMessageId());
            } else {
                params.put("state", MessageStateEnum.FAILURE.getCode());
                params.put("cause", result.getCause());
                params.put("retryCount",messageEntity.getRetryCount()-1);
                params.put("retryNextAt", new Date(System.currentTimeMillis() + MqClientConstants.NEXT_RETRY_GAP));
            }
        } catch (Exception e) {
            log.error("Fail to send the message, messageSentApplication: {}", messageProducerInbound, e);
            params.put("state", MessageStateEnum.EXCEPTION.getCode());
            params.put("cause", e.getMessage());
            params.put("retryCount",messageEntity.getRetryCount()-1);
            params.put("retryNextAt", new Date(System.currentTimeMillis() + MqClientConstants.NEXT_RETRY_GAP));
        }
        if (!MessagePersistModeEnum.BURN_BEFORE_SEND.getCode().equals(messageEntity.getPersistMode())) {
            params.put("id", messageEntity.getId());
            mqProducerMessageMapper.updateMessageState(params);
            log.info("Updating the message status with param: {}", params);
        }
    }
}
