package io.anyway.bigbang.framework.mqclient.service.impl;

import io.anyway.bigbang.framework.mqclient.dao.MqClientIdempotentRepository;
import io.anyway.bigbang.framework.mqclient.domain.PurgeJobContext;
import io.anyway.bigbang.framework.mqclient.domain.PurgeJobType;
import io.anyway.bigbang.framework.mqclient.dao.MqClientMessageRepository;
import io.anyway.bigbang.framework.mqclient.domain.MqClientConfig;
import io.anyway.bigbang.framework.mqclient.utils.MqUtils;
import io.anyway.bigbang.framework.mqclient.utils.MqClientConstants;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@Slf4j
public class MqPurgeJobService {
    @Resource
    private MqClientMessageRepository mqClientMessageRepository;

    @Resource
    private MqClientIdempotentRepository mqClientIdempotentRepository;

    @Resource
    private MqClientConfig mqClientConfig;

    public void purge(PurgeJobContext purgeJobContext) {
        Long nowParKey = MqUtils.evalPartitionKey(new Date());

        log.info("Incoming purgeJobContext={}, partition key now is: {}", purgeJobContext, nowParKey);

        if (purgeJobContext.getMonthAhead() < MqClientConstants.PURGE_JOB_TIME_AHEAD) {
            log.error("MQ Client Message Purge job requested monthAhead={} is illegal, configured is: {}",
                    purgeJobContext.getMonthAhead(), MqClientConstants.PURGE_JOB_TIME_AHEAD);
            return;
        }

        Long partitionKey = nowParKey - purgeJobContext.getMonthAhead();
        Map<String, Object> params = Maps.newHashMap();
        params.put("partitionKey", partitionKey);

        int size;
        if (PurgeJobType.CLIENT_MESSAGE_PURGE_JOB.equals(purgeJobContext.getPurgeJobType())) {
            size = mqClientMessageRepository.purgeMessageByPartitionKey(params);
        } else if (PurgeJobType.IDEMPOTENT_PURGE_JOB.equals(purgeJobContext.getPurgeJobType())) {
            size = mqClientIdempotentRepository.purgeMessageByPartitionKey(params);
        } else {
            log.error("Illegal purge job type={}", purgeJobContext.getPurgeJobType());
            return;
        }

        log.info("Successfully purged records size={}, with partition key={}, mq purge param={}",
                size, partitionKey, params);
    }
}
