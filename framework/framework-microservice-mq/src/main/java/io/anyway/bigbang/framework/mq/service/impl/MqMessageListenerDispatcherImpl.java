package io.anyway.bigbang.framework.mq.service.impl;

import io.anyway.bigbang.framework.gray.GrayContextHolder;
import io.anyway.bigbang.framework.header.HeaderContextHolder;
import io.anyway.bigbang.framework.mq.config.MqClientProperties;
import io.anyway.bigbang.framework.mq.constant.MqTypeEnum;
import io.anyway.bigbang.framework.mq.domain.*;
import io.anyway.bigbang.framework.mq.service.MqMessageIdempotentManager;
import io.anyway.bigbang.framework.mq.service.MqMessageListenerDispatcher;
import io.anyway.bigbang.framework.mq.service.MqMessageRouteForward;
import io.anyway.bigbang.framework.mq.utils.MqMessageContextHolder;
import io.anyway.bigbang.framework.utils.JsonUtil;
import com.google.common.base.Joiner;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;


@Slf4j
public class MqMessageListenerDispatcherImpl implements MqMessageListenerDispatcher ,BeanPostProcessor, MeterBinder {

    @Resource
    private MqMessageRouteForward mqMessageGrayRouter;

    @Resource
    private MqClientProperties mqClientProperties;

    @Resource
    private MqMessageIdempotentManager mqMessageIdempotentValidator;

    private MeterRegistry meterRegistry;

    private volatile Map<String, Counter> consumerCounterMap= new HashMap<>();

    @Override
    public void onMessage(MessageListenerInbound messageListenerInbound, MessageListenerDefinition messageListenerDefinition) {
        try {
            MessageHeader messageHeader = messageListenerInbound.getMessageHeader();
            messageListenerInbound.setTransactionId(messageHeader.getTransactionId());
            if (!StringUtils.isEmpty(messageHeader.getTraceId())) {
                HeaderContextHolder.addHeader("X-Trace-Id", messageHeader.getTraceId());
            }
            GrayContextHolder.setGrayContext(messageHeader.getGrayContext());
            if (mqMessageGrayRouter.isNeededForward(messageHeader.getGrayContext())) {
                mqMessageGrayRouter.doRouteForward(messageListenerInbound, messageListenerDefinition);
            }
            Optional<Object> optional= Optional.empty();
            if(mqClientProperties.isIdempotentOn()) {
                optional = mqMessageIdempotentValidator.tryIdempotent(messageListenerInbound);
                if (!optional.isPresent()) {
                    log.info("Rejected the duplicated message: {}", messageListenerInbound);
                    return;
                }
            }
            try {
                MqMessageContextHolder.setContext(messageListenerInbound);
                for (MessageListenerInvoker each : messageListenerDefinition.getInvokerList()) {
                    doInvoke(each, messageListenerInbound);
                }
                doMqConsumerMetrics(messageListenerInbound.getDestination(),"success");
            }catch (RuntimeException e){
                log.error("invoke message listener error,messageListenerInbound: {}",messageListenerInbound,e);
                if(mqClientProperties.isIdempotentOn()) {
                    mqMessageIdempotentValidator.releaseIdempotent(optional.get());
                }
                doMqConsumerMetrics(messageListenerInbound.getDestination(),"failure");
                throw e;
            }
        }
        finally {
            HeaderContextHolder.removeHeader("X-Trace-Id");
            MqMessageContextHolder.remove();
            GrayContextHolder.removeGrayContext();
        }
    }

    public MessageListenerDefinition findMessageListenerDefinition(MqTypeEnum mqType,String id){
        if(messageListenerDefinitionMap.containsKey(mqType)) {
            return messageListenerDefinitionMap.get(mqType).get(id);
        }
        return null;
    }

    private void doInvoke(MessageListenerInvoker invoker, MessageListenerInbound messageListenerInbound) {
        Method method = invoker.getMethod();
        Object instance = invoker.getInstance();
        Object arg;
        if(invoker.getParameterType() == MessageListenerInbound.class){
            arg= messageListenerInbound;
        }
        else if(invoker.getParameterType() == String.class){
            arg= messageListenerInbound.getMessageBody();
        }
        else{
            arg= JsonUtil.fromString2Object(messageListenerInbound.getMessageBody(),invoker.getParameterType());
            if(MessageListenerContextAware.class.isAssignableFrom(invoker.getParameterType())){
                ((MessageListenerContextAware)arg).setMessageId(messageListenerInbound.getMessageId());
                ((MessageListenerContextAware)arg).setTransactionId(messageListenerInbound.getTransactionId());
            }
        }
        ReflectionUtils.invokeMethod(method,instance,arg);
    }

    //TODO change @Import(SpringFactoryImportSelector)
    @Override
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


    @PostConstruct
    public void init(){
        log.info("messageListenerDefinitionMap: {}",messageListenerDefinitionMap);
    }

    @Override
    public Collection<MessageListenerDefinition> getMessageListenerDefinitionList(MqTypeEnum mqType) {
        return messageListenerDefinitionMap.get(mqType).values();
    }

    private void doMqConsumerMetrics(String topic,String state){
        String counterName= "mq."+state+".consumer."+topic;
        Counter counter= consumerCounterMap.get(counterName);
        if(counter==null){
            synchronized (consumerCounterMap) {
                if((counter=consumerCounterMap.get(counterName))== null) {
                    counter = Counter.builder(counterName).register(meterRegistry);
                    consumerCounterMap.put(counterName,counter);
                }
            }
        }
        counter.increment();
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
    }

}