package io.anyway.bigbang.framework.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.joran.spi.DefaultClass;
import io.anyway.bigbang.framework.utils.JsonUtil;
import net.logstash.logback.composite.JsonProviders;
import net.logstash.logback.composite.loggingevent.LoggingEventJsonProviders;
import net.logstash.logback.composite.loggingevent.LoggingEventPatternJsonProvider;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;

import java.util.Map;
import java.util.Properties;

public class XLoggingEventCompositeJsonEncoder extends LoggingEventCompositeJsonEncoder {

    @Override
    @DefaultClass(LoggingEventJsonProviders.class)
    public void setProviders(JsonProviders<ILoggingEvent> jsonProviders) {
        jsonProviders.getProviders().stream().anyMatch(provider-> {
            if(provider instanceof LoggingEventPatternJsonProvider){
                Properties properties= LoggingExtensionConverterManager.getExtensionProperties();
                String pattern= ((LoggingEventPatternJsonProvider)provider).getPattern();
                Map<String,Object> patternJson= JsonUtil.fromString2Map(pattern);
                ClassLoader classLoader= Thread.currentThread().getContextClassLoader();
                for(String each: properties.stringPropertyNames()){
                    if(!patternJson.containsKey(each)) {
                        String clsName= PatternLayout.defaultConverterMap.get(each);
                        if(clsName!= null){
                            try {
                                Class clazz= classLoader.loadClass(clsName);
                                if(LoggingPatternJsonSerializable.class.isAssignableFrom(clazz) ) {
                                    patternJson.put(each, "#tryJson{"+properties.getProperty(each)+"}");
                                    continue;
                                }
                            } catch (Exception e) {}
                        }
                        patternJson.put(each, properties.getProperty(each));
                    }
                }
                ((LoggingEventPatternJsonProvider)provider).setPattern(JsonUtil.fromObject2String(patternJson));
                return true;
            }
            return false;
        });
        super.setProviders(jsonProviders);
    }
}
