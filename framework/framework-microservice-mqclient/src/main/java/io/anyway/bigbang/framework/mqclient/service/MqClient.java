package io.anyway.bigbang.framework.mqclient.service;

import io.anyway.bigbang.framework.mqclient.domain.MqSendOption;

public interface MqClient {

    void send(MqSendOption mqSendOption);
}
