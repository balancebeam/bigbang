package io.anyway.bigbang.framework.mq.service;

import io.anyway.bigbang.framework.gray.GrayContext;
import io.anyway.bigbang.framework.mq.domain.MessageListenerDefinition;
import io.anyway.bigbang.framework.mq.domain.MessageListenerInbound;

public interface MqMessageRouteForward {

    boolean isNeededForward(GrayContext grayContext);

    void doRouteForward(MessageListenerInbound messageListenerInbound, MessageListenerDefinition messageListenerDefinition);
}
