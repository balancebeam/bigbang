package io.anyway.bigbang.framework.kernel.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import org.slf4j.Marker;

import java.util.HashMap;
import java.util.Map;

public class InheritableThreadLoggingEvent implements ILoggingEvent {

    private Map<String,String> properties= new HashMap<>();

    private ILoggingEvent proxy;

    public InheritableThreadLoggingEvent(ILoggingEvent event){
        this.proxy= event;
    }

    @Override
    public String getThreadName() {
        return proxy.getThreadName();
    }

    @Override
    public Level getLevel() {
        return proxy.getLevel();
    }

    @Override
    public String getMessage() {
        return proxy.getMessage();
    }

    @Override
    public Object[] getArgumentArray() {
        return proxy.getArgumentArray();
    }

    @Override
    public String getFormattedMessage() {
        return proxy.getFormattedMessage();
    }

    @Override
    public String getLoggerName() {
        return proxy.getLoggerName();
    }

    @Override
    public LoggerContextVO getLoggerContextVO() {
        return proxy.getLoggerContextVO();
    }

    @Override
    public IThrowableProxy getThrowableProxy() {
        return proxy.getThrowableProxy();
    }

    @Override
    public StackTraceElement[] getCallerData() {
        return proxy.getCallerData();
    }

    @Override
    public boolean hasCallerData() {
        return proxy.hasCallerData();
    }

    @Override
    public Marker getMarker() {
        return proxy.getMarker();
    }

    @Override
    public Map<String, String> getMDCPropertyMap() {
        return proxy.getMDCPropertyMap();
    }

    @Override
    public Map<String, String> getMdc() {
        return proxy.getMDCPropertyMap();
    }

    @Override
    public long getTimeStamp() {
        return proxy.getTimeStamp();
    }

    @Override
    public void prepareForDeferredProcessing() {
        proxy.prepareForDeferredProcessing();
    }

    public void addProperty(String key,String value){
        properties.put(key,value);
    }

    public boolean containProperty(String key){
        return properties.containsKey(key);
    }

    public String getProperty(String key){
        return properties.get(key);
    }
}
