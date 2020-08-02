package io.anyway.bigbang.framework.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.ArrayList;
import java.util.List;

public class InheritableThreadAsyncAppender extends AsyncAppender {

    private static List<InheritableThreadClassicConverter> inheritableThreadClassicConverters = new ArrayList<>();

    static void addInheritableThreadClassicConverter(InheritableThreadClassicConverter converter) {
        inheritableThreadClassicConverters.add(converter);
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        InheritableThreadLoggingEvent proxy= new InheritableThreadLoggingEvent(eventObject);
        for(InheritableThreadClassicConverter each: inheritableThreadClassicConverters){
            proxy.addProperty(each.getInheritableThreadKey(),each.getInheritableThreadValue(eventObject));
        }
        super.append(proxy);
    }

}
