package io.anyway.bigbang.framework.mq.service;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;

public interface MqMessageListener extends ApplicationListener<ApplicationStartedEvent>, DisposableBean {

    default void onApplicationEvent(ApplicationStartedEvent event){
        start();
    }

    default void destroy() throws Exception{
        stop();
    }

    void start();

    void stop();
}
