package io.anyway.bigbang.framework.core.logging.converter;

import io.anyway.bigbang.framework.core.security.SecurityContextHolder;
import io.anyway.bigbang.framework.core.security.UserDetail;
import io.anyway.bigbang.framework.core.logging.InheritableThreadClassicConverter;
import org.springframework.util.StringUtils;

public class UserDetailConverter extends InheritableThreadClassicConverter {

    @Override
    public String getInheritableThreadValue() {
        UserDetail userDetail= SecurityContextHolder.getUserDetail();
        if(userDetail!= null){
            return (StringUtils.isEmpty(userDetail.getType())? "" : userDetail.getType()+"-")+ userDetail.getUid();
        }
        return "N/A";
    }
}
