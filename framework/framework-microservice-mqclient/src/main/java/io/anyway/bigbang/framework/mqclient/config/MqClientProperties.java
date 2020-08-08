package io.anyway.bigbang.framework.mqclient.config;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;


@Setter
@Getter
@ToString

@ConfigurationProperties(prefix = "spring.rmq-client")
public class MqClientProperties {

    //	private String mqClientIdempotentTableName= "mq_client_idempotent";
    private int workQueueCorePoolSize = 5;
    private int workQueueMaxPoolSize = 5;
    private int workQueueCapacity = 2000;
    private int workQueueKeepAliveSeconds = 300;

    private String producerGroupName;

    private Map<String, String> consumerGroupTopicConf = Maps.newConcurrentMap();
    private Map<String, List<String>> tagsWhitelistConf = Maps.newConcurrentMap();
    private Map<String, List<String>> listenedTagsConf = Maps.newConcurrentMap();

    private String nameSrv;
}
