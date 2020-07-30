package io.anyway.bigbang.framework.kernel.logging.converter;


import ch.qos.logback.classic.spi.ILoggingEvent;
import io.anyway.bigbang.framework.kernel.logging.InheritableThreadClassicConverter;
import io.anyway.bigbang.framework.kernel.security.SecurityContext;
import io.anyway.bigbang.framework.kernel.security.UserDetail;
import org.springframework.util.StringUtils;

public class UserDetailConverter extends InheritableThreadClassicConverter {

    @Override
    public String getInheritableThreadValue(ILoggingEvent event) {
        UserDetail userDetail= SecurityContext.getUserDetail();
        if(userDetail!= null){
            return (StringUtils.isEmpty(userDetail.getType())? "" : userDetail.getType()+"-")+ userDetail.getUid();
        }
        return "";
    }
}
