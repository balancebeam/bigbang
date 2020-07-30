package io.anyway.bigbang.framework.core.logging;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public abstract class InheritableThreadClassicConverter extends ClassicConverter{

    final public String getInheritableThreadKey(){
        return this.getClass().getName();
    }

    public abstract String getInheritableThreadValue();

    @Override
    final public String convert(ILoggingEvent event) {

        if(event instanceof InheritableThreadLoggingEvent){
            InheritableThreadLoggingEvent proxy= (InheritableThreadLoggingEvent)event;
            if(proxy.containProperty(getInheritableThreadKey())){
                return proxy.getProperty(getInheritableThreadKey());
            }
        }
        return getInheritableThreadValue();
    }

}
