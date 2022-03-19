package io.anyway.bigbang.framework.mq.service.impl;

import io.anyway.bigbang.framework.mq.dao.MqConsumerIdempotentMapper;
import io.anyway.bigbang.framework.mq.domain.MessageListenerInbound;
import io.anyway.bigbang.framework.mq.entity.MqClientIdempotentEntity;
import io.anyway.bigbang.framework.mq.service.MqMessageIdempotentManager;
import io.anyway.bigbang.framework.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Optional;

@Slf4j
public class MqMessageIdempotentManagerImpl implements MqMessageIdempotentManager<MqClientIdempotentEntity> {

    @Resource
    private MqConsumerIdempotentMapper mqConsumerIdempotentMapper;

    @Override
    public Optional<MqClientIdempotentEntity> tryIdempotent(MessageListenerInbound messageListenerInbound) {
        MqClientIdempotentEntity mqClientIdempotentEntity= new MqClientIdempotentEntity();
        mqClientIdempotentEntity.setMqType(messageListenerInbound.getMqType().getCode())
                .setTransactionId(messageListenerInbound.getMessageHeader().getTransactionId())
                .setMessageId(messageListenerInbound.getMessageId())
                .setMessageHeader(JsonUtil.fromObject2String(messageListenerInbound.getMessageHeader()))
                .setMessageBody(messageListenerInbound.getMessageBody())
                .setTags(StringUtils.isEmpty(messageListenerInbound.getTags())? "": messageListenerInbound.getTags())
                .setDestination(messageListenerInbound.getDestination())
                .setAttribute(JsonUtil.fromObject2String(messageListenerInbound.getAttribute()));
        int affectCount= mqConsumerIdempotentMapper.insert(mqClientIdempotentEntity);
        log.info("Insert a MqClientIdempotentEntity: {} , success: {}",mqClientIdempotentEntity,affectCount==1);
        return affectCount==0 ? Optional.empty(): Optional.of(mqClientIdempotentEntity);
    }

    @Override
    public void releaseIdempotent(MqClientIdempotentEntity idempotentEntity) {
        mqConsumerIdempotentMapper.deleteById(idempotentEntity.getId());
    }
}
