package io.anyway.bigbang.framework.mq.utils;

import com.alibaba.ttl.TransmittableThreadLocal;
import io.anyway.bigbang.framework.mq.domain.MessageHeader;
import io.anyway.bigbang.framework.mq.domain.MessageListenerInbound;

public abstract class MqMessageContextHolder {

    private MqMessageContextHolder(){}

    private static ThreadLocal<MessageListenerInbound> holder= new TransmittableThreadLocal<>();

    public static MessageHeader getMessageHeader() {
        return holder.get()!=null ? holder.get().getMessageHeader() : null;
    }

    public static MessageListenerInbound getMessageListenerInbound() {
        return holder.get()!=null ? holder.get() : null;
    }

    public static void setContext(MessageListenerInbound inbound){
        holder.set(inbound);
    }

    public static void remove(){
        holder.remove();
    }
}
