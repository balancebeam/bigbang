package io.anyway.bigbang.framework.mqclient.service.impl;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;

@Setter
@Getter
@ToString
public class MqMessageConsumer {

    private DefaultMQPushConsumer defaultMQPushConsumer;
}
