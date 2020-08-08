package io.anyway.bigbang.framework.mqclient.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.anyway.bigbang.framework.mqclient.dao.MqClientMessageRepository;
import io.anyway.bigbang.framework.mqclient.domain.MessageClientStatus;
import io.anyway.bigbang.framework.mqclient.domain.MessagePersistModeStatus;
import io.anyway.bigbang.framework.mqclient.domain.MqClientConfig;
import io.anyway.bigbang.framework.mqclient.domain.MqClientMessageContext;
import io.anyway.bigbang.framework.mqclient.entity.MqClientMessageEntity;
import io.anyway.bigbang.framework.mqclient.utils.MqClientConstants;
import io.anyway.bigbang.framework.mqclient.utils.MqUtils;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
public class MqClientFailover {

    @Resource
    private MqClientMessageRepository mqClientMessageRepository;

    @Resource
    private MqClientImpl mqClient;

    @Resource
    private MqClientConfig mqClientConfig;

    public void failover(int size) {
        Long[] evalValidQueryPartitionKeys = MqUtils.evalValidQueryPartitionKeys(new Date());
        List<Long> partitionKeyList = Lists.newArrayList();
        partitionKeyList.add(evalValidQueryPartitionKeys[0]);
        partitionKeyList.add(evalValidQueryPartitionKeys[1]);

        Map<String, Object> params = Maps.newHashMap();
        params.put("statusList", Arrays.asList(MessageClientStatus.NEW.name(), MessageClientStatus.FAILED.name()));
        params.put("partitionKeyList", partitionKeyList);
        params.put("retryCount", MqClientConstants.MAX_RETRY_TIMES);
        params.put("nextTryAt", new Date());
        params.put("size", size);
        List<MqClientMessageEntity> failOverMessages = mqClientMessageRepository.findReSendingMessages(params);

        log.info("MqClientFailover.failover, list size={}, batchSize={}", failOverMessages.size(), size);

        for (MqClientMessageEntity messageEntity : failOverMessages) {
            MqClientMessageContext mqClientMessageContext = new MqClientMessageContext();
            mqClientMessageContext.setMqClientMessageEntity(messageEntity);
            mqClientMessageContext.setPersistMode(MessagePersistModeStatus.of(messageEntity.getPersistMode()));
            mqClientMessageContext.setFailoverCtx(true);
            mqClient.doSend(mqClientMessageContext);
        }

        log.info("End executing the failover job.");
    }
}
