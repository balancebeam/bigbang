package io.anyway.bigbang.framework.mq.service;

import io.anyway.bigbang.framework.mq.constant.MessageStateEnum;
import io.anyway.bigbang.framework.mq.dao.MqProducerMessageMapper;
import io.anyway.bigbang.framework.mq.entity.MqClientMessageEntity;
import io.anyway.bigbang.framework.mq.service.impl.MqProducerClientImpl;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MqProducerClientManager {

    @Resource
    private MqProducerMessageMapper mqProducerMessageMapper;

    @Resource
    private MqProducerClientImpl mqClient;

    public void failover() {
        List<MqClientMessageEntity> failOverMessages = mqProducerMessageMapper.findReSendingMessages();
        for (MqClientMessageEntity messageEntity : failOverMessages) {
            mqClient.doSend(messageEntity);
        }
    }

    public void purge() {
        Map<String,Object> param= new LinkedHashMap<>();
        param.put("state", MessageStateEnum.SUCCESS.getCode());
        Calendar calendar= Calendar.getInstance();
        calendar.add(Calendar.MONTH,-1);
        param.put("date",calendar.getTime());
        mqProducerMessageMapper.purge(param);
    }
}
