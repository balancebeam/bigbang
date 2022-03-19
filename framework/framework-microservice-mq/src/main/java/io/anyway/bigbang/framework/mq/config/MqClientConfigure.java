package io.anyway.bigbang.framework.mq.config;

import io.anyway.bigbang.framework.mutex.config.MutexConfigure;
import io.anyway.bigbang.framework.mq.annotation.MqConsumerConditional;
import io.anyway.bigbang.framework.mq.annotation.MqListener;
import io.anyway.bigbang.framework.mq.annotation.MqProducerConditional;
import io.anyway.bigbang.framework.mq.constant.MqTypeEnum;
import io.anyway.bigbang.framework.mq.controller.MqConsumerController;
import io.anyway.bigbang.framework.mq.controller.MqProducerController;
import io.anyway.bigbang.framework.mq.domain.MessageListenerDefinition;
import io.anyway.bigbang.framework.mq.domain.MessageListenerInvoker;
import io.anyway.bigbang.framework.mq.schedule.MqMutexSchedule;
import io.anyway.bigbang.framework.mq.service.*;
import io.anyway.bigbang.framework.mq.service.impl.*;
import io.anyway.bigbang.framework.utils.SpringUtil;
import com.google.common.base.Joiner;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;

import static io.anyway.bigbang.framework.mq.service.MqMessageListenerDispatcher.messageListenerDefinitionMap;

@Configuration
@MapperScan({"io.anyway.bigbang.framework.mq.dao"})
@EnableConfigurationProperties(MqClientProperties.class)
@ImportAutoConfiguration({
        MqClientConfigure.MqProducerConfigure.class,
        MqClientConfigure.MqConsumerConfigure.class,
        MqClientConfigure.MutexScheduleConfigure.class
})
public class MqClientConfigure {

    @Configuration
    public static class MqProducerConfigure{

        @Resource
        private MqClientProperties mqClientProperties;

        @Bean
        public MqProducerClient createMqProducerClient(){
            return new MqProducerClientImpl();
        }

        @Bean
        @MqProducerConditional(MqTypeEnum.ACTIVEMQ)
        public MqMessageProducer createMqMessageActivemqProducer(){
            return new MqMessageActivemqProducerImpl();
        }

        @Bean
        @MqProducerConditional(MqTypeEnum.RABBITMQ)
        public MqMessageProducer createMqMessageRabbitmqProducer(){
            return new MqMessageRabbitmqProducerImpl();
        }

        @Bean
        @MqProducerConditional(MqTypeEnum.KAFKA)
        public MqMessageProducer createMqMessageKafkaProducer(){
            return new MqMessageKafkaProducerImpl();
        }

        @Bean
        @MqProducerConditional(MqTypeEnum.ROCKETMQ)
        public MqMessageProducer createMqMessageRocketmqProducer(){
            return new MqMessageRocketmqProducerImpl();
        }

        @Bean
        @MqProducerConditional(MqTypeEnum.PULSAR)
        public MqMessageProducer createMqMessagePulsarProducer(){
            return new MqMessagePulsarProducerImpl();
        }

        @Bean
        public MqProducerClientManager createMqProducerClientManager(){
            return new MqProducerClientManager();
        }

        @Bean
        public MqProducerController createMqProducerController(){
            return new MqProducerController();
        }
    }

    @Configuration
    public static class MqConsumerConfigure implements BeanPostProcessor {

        @Resource
        private MqClientProperties mqClientProperties;

        @LoadBalanced
        @Bean("mqLoadBalanceRestTemplate")
        public RestTemplate mqLoadBalanceRestTemplate() {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(30000);
            factory.setReadTimeout(30000);
            RestTemplate restTemplate = new RestTemplate(factory);
            return restTemplate;
        }

        @Bean
        public MqMessageRouteForward createMqMessageGrayRouter(){
            return new MqMessageRouteForwardImpl();
        }

        @Bean
        @ConditionalOnMissingBean
        public MqMessageIdempotentManager createMqMessageIdempotentManager(){
            return new MqMessageIdempotentManagerImpl();
        }

        @Bean
        public MqMessageListenerDispatcher createMqMessageListenerDispatcher(){
            return new MqMessageListenerDispatcherImpl();
        }

        @Bean
        @MqConsumerConditional(MqTypeEnum.ACTIVEMQ)
        public MqMessageListener createMqMessageActivemqListener(){
            return new MqMessageActivemqListenerImpl();
        }

        @Bean
        @MqConsumerConditional(MqTypeEnum.RABBITMQ)
        public MqMessageListener createMqMessageRabbitmqListener(){
            return new MqMessageRabbitmqListenerImpl();
        }

        @Bean
        @MqConsumerConditional(MqTypeEnum.KAFKA)
        public MqMessageListener createMqMessageKafkaListener(){
            return new MqMessageKafkaListenerImpl();
        }

        @Bean
        @MqConsumerConditional(MqTypeEnum.ROCKETMQ)
        public MqMessageListener createMqMessageRocketmqListener(){
            return new MqMessageRocketmqListenerImpl();
        }

        @Bean
        @MqConsumerConditional(MqTypeEnum.PULSAR)
        public MqMessageListener createMqMessagePulsarListener(){
            return new MqMessagePulsarListenerImpl();
        }

        @Bean
        public MqConsumerClientManager createMqConsumerClientManager(){
            return new MqConsumerClientManager();
        }

        @Bean
        public MqConsumerController createMqConsumerController(){
            return new MqConsumerController();
        }

        //TODO change @Import(SpringFactoryImportSelector)
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            Object target = SpringUtil.getProxyTarget(bean);
            for (Method method : target.getClass().getDeclaredMethods()) {
                MqListener listener = method.getAnnotation(MqListener.class);
                if (listener != null) {
                    List<String> tags= Arrays.asList(listener.tags());
                    Collections.sort(tags);
                    String key= listener.group()+"@"+listener.value()+"["+(tags.isEmpty()? "*" : Joiner.on("||").join(tags))+"]";
                    MqTypeEnum mqType= mqClientProperties.getMqType();
                    if(listener.mqType()!=MqTypeEnum.DEFAULT){
                        mqType= listener.mqType();
                    }
                    if(!messageListenerDefinitionMap.containsKey(mqType)){
                        messageListenerDefinitionMap.put(mqType,new LinkedHashMap<>());
                    }
                    Map<String,MessageListenerDefinition> definitionMap = messageListenerDefinitionMap.get(mqType);
                    MessageListenerDefinition wrapper= definitionMap.get(key);
                    if(wrapper== null){
                        wrapper= new MessageListenerDefinition();
                        wrapper.setId(key).setDestination(listener.value()).setTags(tags).setGroup(listener.group());
                        definitionMap.put(key,wrapper);
                    }
                    MessageListenerInvoker invoker= new MessageListenerInvoker();
                    invoker.setInstance(bean);
                    invoker.setMethod(method);
                    if(method.getParameterTypes().length!=1) {
                        throw new IllegalArgumentException("method "+method+" parameter was incorrect.");
                    }
                    invoker.setParameterType(method.getParameterTypes()[0]);
                    ReflectionUtils.makeAccessible(method);
                    wrapper.getInvokerList().add(invoker);
                }
            }
            return bean;
        }
    }

    @Configuration
    @EnableScheduling
    @ImportAutoConfiguration(MutexConfigure.class)
    @ConditionalOnProperty(name = "spring.reliable-mq.mutex-schedule",havingValue = "true",matchIfMissing = false)
    public static class MutexScheduleConfigure {

        @Bean
        public MqMutexSchedule createMqMutexSchedule(){
            return new MqMutexSchedule();
        }
    }
}
