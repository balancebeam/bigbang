package io.anyway.bigbang.framework.apm;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.alibaba.ttl.TransmittableThreadLocal;
import io.anyway.bigbang.framework.logging.InheritableThreadClassicConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultLoggingTraceIdConverter extends InheritableThreadClassicConverter {

    final public static ThreadLocal<String> LOCAL_TRACE_HOLDER= new TransmittableThreadLocal<>();

    @Override
    public String getInheritableThreadValue(ILoggingEvent event) {
        return LOCAL_TRACE_HOLDER.get()!=null? LOCAL_TRACE_HOLDER.get(): "";
    }

}
