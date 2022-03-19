package io.anyway.bigbang.framework.mq.service;

import io.anyway.bigbang.framework.mq.domain.MessageListenerInbound;

import java.util.Optional;

public interface MqMessageIdempotentManager<T> {

    Optional<T> tryIdempotent(MessageListenerInbound messageListenerInbound);

    void releaseIdempotent(T inData);
}
