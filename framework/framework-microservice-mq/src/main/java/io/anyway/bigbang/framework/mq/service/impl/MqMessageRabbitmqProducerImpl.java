package io.anyway.bigbang.framework.mq.service.impl;

import io.anyway.bigbang.framework.mq.constant.MqTypeEnum;
import io.anyway.bigbang.framework.mq.domain.MessageProducerInbound;
import io.anyway.bigbang.framework.mq.domain.MessageProducerOutbound;
import io.anyway.bigbang.framework.mq.service.MqMessageProducer;

public class MqMessageRabbitmqProducerImpl implements MqMessageProducer {

    @Override
    public MessageProducerOutbound send(MessageProducerInbound inbound) throws Exception {
        return null;
    }

    @Override
    public MqTypeEnum type() {
        return MqTypeEnum.RABBITMQ;
    }
}
