package io.anyway.bigbang.framework.mqclient.builder;

import io.anyway.bigbang.framework.mqclient.domain.MqClientConfig;
import io.anyway.bigbang.framework.mqclient.domain.MqType;
import io.anyway.bigbang.framework.mqclient.service.impl.MqMessageConsumer;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;


@Slf4j
@Configuration
public class MqConsumerInitializer {

    @Bean
    public RMQMessageConsumerBuilder createRMQMessageConsumerBuilder() {
        return new RMQMessageConsumerBuilder();
    }

    @Bean
    public List<MqMessageConsumer> mqMessageConsumerList(MqClientConfig mqClientConfig,
                                                         RMQMessageConsumerBuilder rmqMessageConsumerBuilder) {
        Map<String, RMQMessageConsumerBuilder> messageConsumerBuilderMap = Maps.newHashMap();
        messageConsumerBuilderMap.put(MqType.RMQ.name(), rmqMessageConsumerBuilder);

        return messageConsumerBuilderMap.get(mqClientConfig.getMqType().name()).build(mqClientConfig);
    }
}
