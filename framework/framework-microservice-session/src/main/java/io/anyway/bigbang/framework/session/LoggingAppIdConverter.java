package io.anyway.bigbang.framework.session;


import ch.qos.logback.classic.spi.ILoggingEvent;
import io.anyway.bigbang.framework.logging.InheritableThreadClassicConverter;

import java.util.Optional;

public class LoggingAppIdConverter extends InheritableThreadClassicConverter {

    @Override
    public String getInheritableThreadValue(ILoggingEvent event) {
        Optional<UserDetailContext> userDetailContext= SessionContextHolder.getUserDetailContext();
        if(userDetailContext.isPresent()){
            UserDetailContext ctx= userDetailContext.get();
            return ctx.getAppId();
        }
        return "";
    }
}
