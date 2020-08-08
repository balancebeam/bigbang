package io.anyway.bigbang.framework.mqclient.builder;


import io.anyway.bigbang.framework.mqclient.domain.MqClientConfig;
import io.anyway.bigbang.framework.mqclient.service.impl.MqMessageConsumer;

import java.util.List;

public interface MqMessageConsumerBuilder {
    List<MqMessageConsumer> build(MqClientConfig mqClientConfig);
}
