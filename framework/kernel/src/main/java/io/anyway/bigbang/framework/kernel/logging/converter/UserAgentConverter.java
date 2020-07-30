package io.anyway.bigbang.framework.kernel.logging.converter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.anyway.bigbang.framework.kernel.logging.InheritableThreadClassicConverter;
import io.anyway.bigbang.framework.kernel.useragent.UserAgent;
import io.anyway.bigbang.framework.kernel.useragent.UserAgentContext;

public class UserAgentConverter extends InheritableThreadClassicConverter {

    @Override
    public String getInheritableThreadValue(ILoggingEvent event) {
        UserAgent userAgent= UserAgentContext.getUserAgent();
        if(userAgent!= null){
            return userAgent.getPlatform()+"-" + userAgent.getVersion();
        }
        return "";
    }
}
