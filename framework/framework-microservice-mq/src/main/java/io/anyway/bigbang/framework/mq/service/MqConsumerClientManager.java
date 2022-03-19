package io.anyway.bigbang.framework.mq.service;

import io.anyway.bigbang.framework.mq.dao.MqConsumerIdempotentMapper;
import io.anyway.bigbang.framework.mq.entity.MqClientIdempotentEntity;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MqConsumerClientManager {

    @Resource
    private MqConsumerIdempotentMapper mqConsumerIdempotentMapper;

    public List<MqClientIdempotentEntity> queryAll() {
        return mqConsumerIdempotentMapper.findAll();
    }

    public void purge() {
        Map<String,Object> param= new LinkedHashMap<>();
        Calendar calendar= Calendar.getInstance();
        calendar.add(Calendar.MONTH,-1);
        param.put("date",calendar.getTime());
        mqConsumerIdempotentMapper.purge(param);
    }
}
