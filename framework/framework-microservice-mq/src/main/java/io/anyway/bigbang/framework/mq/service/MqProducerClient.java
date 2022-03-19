package io.anyway.bigbang.framework.mq.service;

import io.anyway.bigbang.framework.mq.domain.MqSendOption;

public interface MqProducerClient {

    void send(MqSendOption mqSendOption);
}
