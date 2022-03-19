package io.anyway.bigbang.framework.mq.service;

import io.anyway.bigbang.framework.mq.constant.MqTypeEnum;
import io.anyway.bigbang.framework.mq.domain.MessageListenerDefinition;
import io.anyway.bigbang.framework.mq.domain.MessageListenerInbound;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public interface MqMessageListenerDispatcher {
    Map<MqTypeEnum,Map<String,MessageListenerDefinition>> messageListenerDefinitionMap = new HashMap<>();

    Collection<MessageListenerDefinition> getMessageListenerDefinitionList(MqTypeEnum mqTypeEnum);

    void onMessage(MessageListenerInbound messageListenerInbound, MessageListenerDefinition messageListenerDefinition);
}
