package io.anyway.bigbang.framework.mqclient.service.impl;

import io.anyway.bigbang.framework.mqclient.dao.MqClientIdempotentRepository;
import io.anyway.bigbang.framework.mqclient.domain.MqClientMessage;
import io.anyway.bigbang.framework.mqclient.entity.MqClientIdempotentEntity;
import io.anyway.bigbang.framework.mqclient.domain.MqClientConfig;
import io.anyway.bigbang.framework.mqclient.service.MqRequestRouter;
import io.anyway.bigbang.framework.mqclient.utils.MqClientConstants;
import io.anyway.bigbang.framework.mqclient.utils.MqUtils;
import com.google.common.collect.Maps;
import io.anyway.bigbang.framework.gray.GrayContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public class MqMessageListenerWrapper {

    @Value("${spring.rmq-client.consumer-idempotent:false}")
    private boolean idempotent;

    @Resource
    private MqClientIdempotentRepository mqClientIdempotentRepository;

    @Autowired(required = false)
    private MqRequestRouter mqRequestRouter = mqClientMessage -> false;

    public void localProcess(MqClientConfig mqClientConfig, MqClientMessage mqClientMessage) {
        List<String> whitelistTags = mqClientConfig.getTagsWhitelistConf().get(mqClientMessage.getDestination());
        if (!idempotent || (whitelistTags != null && !whitelistTags.contains(mqClientMessage.getTags()))) {
            log.info("no need to do idempotent control, directly processing the message and return");
            try {
                mqClientConfig.getMqMessageListener().onMessage(mqClientMessage);
            } catch (Exception e) {
                log.error("Exception happened when process, message={}", mqClientMessage, e);
                throw e;
            }
        } else {
            Map<String, Object> params = Maps.newHashMap();
            params.put("messageKey", mqClientMessage.getMessageKey());
            params.put("mqType", mqClientConfig.getMqType().name());

            log.info("start check mq client idempotent");

            try {
                log.debug("Idempotent param={}", params);
                MqClientIdempotentEntity mqClientIdempotentEntity = mqClientIdempotentRepository.findByMessageKey(params);
                if (mqClientIdempotentEntity != null) {
                    log.info("Exiting message has existed, messageKey={}", mqClientMessage.getMessageKey());
                    return;
                }

                mqClientIdempotentEntity = new MqClientIdempotentEntity();
                mqClientIdempotentEntity.setMessageId(mqClientMessage.getMessageId());
                mqClientIdempotentEntity.setMessageKey(mqClientMessage.getMessageKey());
                mqClientIdempotentEntity.setMessage(
                        mqClientMessage.getMessage().length() > MqClientConstants.MESSAGE_MAX_SIZE ?
                                "Message length too long" : mqClientMessage.getMessage());
                mqClientIdempotentEntity.setMessageType(mqClientMessage.getMessageType());
                mqClientIdempotentEntity.setDestination(mqClientMessage.getDestination());
                mqClientIdempotentEntity.setTags(mqClientMessage.getTags());
                mqClientIdempotentEntity.setPartitionKey(MqUtils.evalPartitionKey(new Date()));
                mqClientIdempotentEntity.setMqType(mqClientConfig.getMqType().name());

                mqClientIdempotentRepository.insert(mqClientIdempotentEntity);

                try {
                    mqClientConfig.getMqMessageListener().onMessage(mqClientMessage);
                } catch (Exception e) {
                    log.error("Exception happened when onMessage and now deleted idempotent record with id={}",
                            mqClientIdempotentEntity.getId(), e);
                    deleteMsg(mqClientIdempotentEntity, mqClientConfig);
                    throw new RuntimeException(new RuntimeException(e));
                }
            } catch (Exception e) {
                if (isUniqueConstraintViolate(e)) {
                    log.warn("Duplicate idempotent item", e);
                    return;
                }
                /**
                 * Handle the DB access failure, in case the idempotent item has been actually inserted
                 */
                log.warn("Exception when inserting idempotent item", e);
                int c = 0;
                while (c++ < 3) {
                    try {
                        MqClientIdempotentEntity mqClientIdempotentEntity =
                                mqClientIdempotentRepository.findByMessageKey(params);
                        if (mqClientIdempotentEntity != null) {
                            log.error("Exception happened when onMessage and now deleted idempotent record with id={}",
                                    mqClientIdempotentEntity.getId(), e);
                            deleteMsg(mqClientIdempotentEntity, mqClientConfig);
                        }
                    } catch (Exception ee) {
                        log.warn("Exception happened when delete msg", e);
                        try {
                            Thread.sleep(100);
                        } catch (Exception ie) {
                        }
                    }
                }
                throw e;
            }
        }
    }

    public void process(MqClientConfig mqClientConfig, MqClientMessage mqClientMessage) {
        log.info("process the message, message_id={}, message_key={}, message={}, with config={}",
                mqClientMessage.getMessageId(),
                mqClientMessage.getMessageKey(),
                mqClientMessage,
                mqClientConfig);

        if(mqClientMessage.getMessageHeader()!=null) {
            GrayContextHolder.setGrayContext(mqClientMessage.getMessageHeader().getGrayContext());
        }
        try {
            // For canary deployment: determine whether we should route the message to other consumer parties
            if (mqRequestRouter.route(mqClientMessage)) {
                return;
            }
            localProcess(mqClientConfig, mqClientMessage);
        }finally {
            GrayContextHolder.removeGrayContext();
        }

    }

    private void deleteMsg(MqClientIdempotentEntity mqClientIdempotentEntity, MqClientConfig mqClientConfig) {
        try {
            Map delParams = Maps.newHashMap();
            delParams.put("id", mqClientIdempotentEntity.getId());
            mqClientIdempotentRepository.deleteById(delParams);
        } catch (Exception e) {
            log.error("Exception happened when deleted idempotent records EXP_IDEM_ID={}",
                    mqClientIdempotentEntity.getId(), e);
        }
    }

    private boolean isUniqueConstraintViolate(Exception e) {
        if (e instanceof DuplicateKeyException) {
            return true;
        } else {
            String errorCode = "";
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                errorCode = e.getCause().getMessage();
            }

            if (StringUtils.isBlank(errorCode) && e.getMessage() != null) {
                errorCode = e.getMessage();
            }
            return errorCode.indexOf("Duplicate entry") >= 0;
        }
    }
}
