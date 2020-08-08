package io.anyway.bigbang.framework.mqclient.config;

import io.anyway.bigbang.framework.mqclient.builder.MqConsumerInitializer;
import io.anyway.bigbang.framework.mqclient.controller.MqClientController;
import io.anyway.bigbang.framework.mqclient.domain.MqClientConfig;
import io.anyway.bigbang.framework.mqclient.domain.MqType;
import io.anyway.bigbang.framework.mqclient.service.MqClient;
import io.anyway.bigbang.framework.mqclient.service.impl.*;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Slf4j
@Configuration
@AutoConfigureBefore(MqConfig.RocketMqConfig.class)
public class MqConfig {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnExpression("'${spring.rmq-client.name-srv:}'.length()==0")
    public MqClient createDefaultMqClient() {
        return mqSendOption -> log.info("dry run: {}", mqSendOption);
    }


    @Configuration
    @MapperScan("io.anyway.bigbang.framework.mqclient.dao")
    @EnableConfigurationProperties(MqClientProperties.class)
    @ImportAutoConfiguration({MqConsumerInitializer.class, RouterConfig.class})
    @ConditionalOnExpression("'${spring.rmq-client.name-srv:}'.length()>0")
    static class RocketMqConfig {

        @Resource
        private MqClientProperties properties;

        @Bean
        public MqMessageDispatcherListener createDispatchMqMessageListener() {
            return new MqMessageDispatcherListener();
        }

        @Bean
        public MqClientConfig mqClientConfig(MqMessageDispatcherListener listener) {
            MqClientConfig config = new MqClientConfig();
            config.setProducerGroupName(properties.getProducerGroupName());
            config.setNameSrv(properties.getNameSrv());
            config.setWorkQueueCapacity(properties.getWorkQueueCapacity());
            config.setWorkQueueCorePoolSize(properties.getWorkQueueCorePoolSize());
            config.setWorkQueueMaxPoolSize(properties.getWorkQueueMaxPoolSize());
            config.setMqType(MqType.RMQ);
            config.setConsumerGroupTopicConf(properties.getConsumerGroupTopicConf());
            config.setListenedTagsConf(properties.getListenedTagsConf());
            config.setTagsWhitelistConf(properties.getTagsWhitelistConf());
            config.setMqMessageListener(listener);
            return config;
        }

        @Bean
        public MqMessageProducer createMqMessageProducer() {
            return new MqMessageProducer();
        }

        @Bean
        public MqClientImpl createMqClientImpl() {
            return new MqClientImpl();
        }

        @Bean
        public MqClientFailover createMqClientFailover() {
            return new MqClientFailover();
        }

        @Bean
        public MqMessageListenerWrapper createMqMessageListenerWrapper() {
            return new MqMessageListenerWrapper();
        }

        @Bean
        public MqPurgeJobService createMqPurgeJobService() {
            return new MqPurgeJobService();
        }

        @Bean
        public MqClientController createMqClientController() {
            return new MqClientController();
        }
    }

}
