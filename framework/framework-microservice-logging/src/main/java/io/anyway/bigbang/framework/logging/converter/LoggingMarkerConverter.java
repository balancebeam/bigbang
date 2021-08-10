package io.anyway.bigbang.framework.logging.converter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.anyway.bigbang.framework.logging.InheritableThreadClassicConverter;

public class LoggingMarkerConverter extends InheritableThreadClassicConverter {

    @Override
    public String getInheritableThreadValue(ILoggingEvent event) {

        if(event.getMarker()!=null){
            return event.getMarker().toString();
        }
        return null;
    }
}
