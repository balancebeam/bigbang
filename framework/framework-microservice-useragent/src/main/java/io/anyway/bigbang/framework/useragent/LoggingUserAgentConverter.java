package io.anyway.bigbang.framework.useragent;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.anyway.bigbang.framework.logging.InheritableThreadClassicConverter;

import java.util.Optional;

public class LoggingUserAgentConverter extends InheritableThreadClassicConverter {

    @Override
    public String getInheritableThreadValue(ILoggingEvent event) {
        Optional<UserAgentContext> userAgentContext= UserAgentContextHolder.getUserAgentContext();
        if(userAgentContext.isPresent()){
            UserAgentContext ctx= userAgentContext.get();
            return ctx.getPlatform()+"-" + ctx.getVersion();
        }
        return "";
    }
}
