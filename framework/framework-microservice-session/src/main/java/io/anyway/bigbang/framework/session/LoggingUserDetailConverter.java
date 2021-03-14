package io.anyway.bigbang.framework.session;


import ch.qos.logback.classic.spi.ILoggingEvent;
import io.anyway.bigbang.framework.logging.InheritableThreadClassicConverter;
import org.springframework.util.StringUtils;

import java.util.Optional;

public class LoggingUserDetailConverter extends InheritableThreadClassicConverter {

    @Override
    public String getInheritableThreadValue(ILoggingEvent event) {
        Optional<UserDetailContext> userDetailContext= SessionContextHolder.getUserDetailContext();
        if(userDetailContext.isPresent()){
            UserDetailContext ctx= userDetailContext.get();
            return (StringUtils.isEmpty(ctx.getType())? "" : ctx.getType()+"-")+ ctx.getUid();
        }
        return "";
    }
}
