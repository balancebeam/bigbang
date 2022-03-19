package io.anyway.bigbang.framework.mq.domain;


public interface MessageListenerContextAware {

    default void setTransactionId(String transactionId){
    }

    default void setMessageId(String messageId){
    }

}
