package io.anyway.bigbang.framework.mqclient.domain;

import io.anyway.bigbang.framework.mqclient.service.MqMessageListener;
import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.rocketmq.client.producer.MessageQueueSelector;

import java.util.List;
import java.util.Map;


@Setter
@Getter
@ToString
public class MqClientConfig {
    private int workQueueCorePoolSize = 5;
    private int workQueueMaxPoolSize = 5;
    private int workQueueCapacity = 2000;
    private int workQueueKeepAliveSeconds = 300;

    private String producerGroupName;

    private Map<String, String> consumerGroupTopicConf = Maps.newConcurrentMap();
    private Map<String, List<String>> tagsWhitelistConf = Maps.newConcurrentMap();
    private Map<String, List<String>> listenedTagsConf = Maps.newConcurrentMap();

    private String nameSrv;

    private MqMessageListener mqMessageListener;

    private MessageQueueSelector customizedMQSelector;

    private MqType mqType;

    private boolean msgListeningOrderly = false;
}
