package io.anyway.bigbang.framework.mq.service;

import io.anyway.bigbang.framework.mq.constant.MqTypeEnum;
import io.anyway.bigbang.framework.mq.domain.MessageProducerInbound;
import io.anyway.bigbang.framework.mq.domain.MessageProducerOutbound;

public interface MqMessageProducer {

    /**
     * 发送消息同步方法
     * @param inbound
     * @return
     * @throws Exception
     */
    MessageProducerOutbound send(MessageProducerInbound inbound) throws Exception;

    /**
     * 消息中间件类型
     * @return
     */
    MqTypeEnum type();

}
