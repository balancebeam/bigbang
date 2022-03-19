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
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Optional;


@Slf4j
public class MqMessageListenerDispatcherImpl implements MqMessageListenerDispatcher {

    @Resource
    private MqMessageRouteForward mqMessageGrayRouter;

    @Resource
    private MqClientProperties mqClientProperties;

    @Resource
    private MqMessageIdempotentManager mqMessageIdempotentValidator;

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
            }catch (Exception e){
                log.error("invoke message listener error,messageListenerInbound: {}",messageListenerInbound,e);
                if(mqClientProperties.isIdempotentOn()) {
                    mqMessageIdempotentValidator.releaseIdempotent(optional.get());
                }
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

    @PostConstruct
    public void init(){
        log.info("messageListenerDefinitionMap: {}",messageListenerDefinitionMap);
    }

    @Override
    public Collection<MessageListenerDefinition> getMessageListenerDefinitionList(MqTypeEnum mqType) {
        return messageListenerDefinitionMap.get(mqType).values();
    }

}
