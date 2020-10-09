package io.anyway.bigbang.framework.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.joran.spi.DefaultClass;
import com.alibaba.fastjson.JSONObject;
import net.logstash.logback.composite.JsonProviders;
import net.logstash.logback.composite.loggingevent.LoggingEventJsonProviders;
import net.logstash.logback.composite.loggingevent.LoggingEventPatternJsonProvider;
import net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder;

import java.util.Properties;

public class XLoggingEventCompositeJsonEncoder extends LoggingEventCompositeJsonEncoder {

    @Override
    @DefaultClass(LoggingEventJsonProviders.class)
    public void setProviders(JsonProviders<ILoggingEvent> jsonProviders) {
        jsonProviders.getProviders().stream().anyMatch(provider-> {
            if(provider instanceof LoggingEventPatternJsonProvider){
                Properties properties= LoggingExtensionConverterManager.getExtensionProperties();
                String pattern= ((LoggingEventPatternJsonProvider)provider).getPattern();
                JSONObject patternJson= JSONObject.parseObject(pattern);
                for(String each: properties.stringPropertyNames()){
                    if(!patternJson.containsKey(each)) {
                        patternJson.put(each, "%".concat(each));
                    }
                }
                ((LoggingEventPatternJsonProvider)provider).setPattern(patternJson.toJSONString());
                return true;
            }
            return false;
        });
        super.setProviders(jsonProviders);
    }
}
