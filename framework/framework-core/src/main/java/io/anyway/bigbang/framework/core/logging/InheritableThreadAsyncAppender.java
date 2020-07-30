package io.anyway.bigbang.framework.core.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import io.anyway.bigbang.framework.core.logging.converter.PlatformConverter;
import io.anyway.bigbang.framework.core.logging.converter.TraceIdConverter;
import io.anyway.bigbang.framework.core.logging.converter.UserDetailConverter;
import io.anyway.bigbang.framework.core.logging.converter.VersionConverter;

import java.util.ArrayList;
import java.util.List;

public class InheritableThreadAsyncAppender extends AsyncAppender {

    private static List<InheritableThreadClassicConverter> inheritableThreadClassicConverters= new ArrayList<>();

    static{
        inheritableThreadClassicConverters.add(new TraceIdConverter());
        inheritableThreadClassicConverters.add(new UserDetailConverter());
        inheritableThreadClassicConverters.add(new PlatformConverter());
        inheritableThreadClassicConverters.add(new VersionConverter());
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        InheritableThreadLoggingEvent proxy= new InheritableThreadLoggingEvent(eventObject);
        for(InheritableThreadClassicConverter each: inheritableThreadClassicConverters){
            proxy.addProperty(each.getInheritableThreadKey(),each.getInheritableThreadValue());
        }
        super.append(proxy);
    }

}
