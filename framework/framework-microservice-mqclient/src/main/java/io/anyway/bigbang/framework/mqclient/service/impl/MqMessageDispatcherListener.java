package io.anyway.bigbang.framework.mqclient.service.impl;

import io.anyway.bigbang.framework.mqclient.domain.MqClientMessage;
import io.anyway.bigbang.framework.mqclient.annotation.Listener;
import io.anyway.bigbang.framework.mqclient.service.MqMessageListener;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Method;
import java.util.*;


public class MqMessageDispatcherListener implements MqMessageListener, BeanPostProcessor {

    private Map<String, List<Invoker>> dispatcher = new HashMap<>();

    @Override
    public void onMessage(MqClientMessage mqClientMessage) {
        String destination = mqClientMessage.getDestination();
        if (dispatcher.containsKey(destination)) {
            doInvoke(destination, mqClientMessage);
        } else if (dispatcher.containsKey("*")) {
            doInvoke("*", mqClientMessage);
        }
    }

    private void doInvoke(String destination, MqClientMessage mqClientMessage) {
        Set<String> messageTags = new HashSet<>();
        for (String tag : mqClientMessage.getTags().split(",")) {
            messageTags.add(tag);
        }
        l:
        for (Invoker each : dispatcher.get(destination)) {
            for (String tag : each.getTags()) {
                if (!messageTags.contains(tag)) {
                    continue l;
                }
            }
            try {
                each.getMethod().invoke(each.getInstance(), mqClientMessage);
            } catch (Exception e) {
                throw new RuntimeException("invoke listener error.", e);
            }
        }
    }


    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Object target = getProxyTarget(bean);
        for (Method each : target.getClass().getDeclaredMethods()) {
            Listener listener = each.getAnnotation(Listener.class);
            if (listener != null) {
                if (!dispatcher.containsKey(listener.value())) {
                    dispatcher.put(listener.value(), new ArrayList<>());
                }
                each.setAccessible(true);
                dispatcher.get(listener.value()).add(new Invoker(listener.tags(), each, bean));
            }
        }
        return bean;
    }

    public static <T> T getProxyTarget(Object proxy) {
        if (!AopUtils.isAopProxy(proxy)) {
            return (T) proxy;
        }
        TargetSource targetSource = ((Advised) proxy).getTargetSource();
        return getTarget(targetSource);
    }

    private static <T> T getTarget(TargetSource targetSource) {
        try {
            return (T) targetSource.getTarget();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Setter
    @Getter
    @ToString
    @AllArgsConstructor
    static class Invoker {
        private String[] tags;
        private Method method;
        private Object instance;
    }
}
