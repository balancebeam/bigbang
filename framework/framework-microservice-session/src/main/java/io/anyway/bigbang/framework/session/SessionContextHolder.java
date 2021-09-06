package io.anyway.bigbang.framework.session;

import com.alibaba.ttl.TransmittableThreadLocal;
import io.anyway.bigbang.framework.header.HeaderContextHolder;
import io.anyway.bigbang.framework.utils.JsonUtil;

import java.util.Optional;

public class SessionContextHolder {

    private SessionContextHolder(){}

    private static Class<? extends UserDetailContext> userDetailClass= DefaultUserDetailContext.class;

    private static ThreadLocal<UserDetailContext> threadLocal= new TransmittableThreadLocal<>();

    public static void setUserDetailClass(Class<? extends UserDetailContext> cls){
        userDetailClass= cls;
    }

    public static <T extends UserDetailContext> Optional<T> getUserDetailContext(){
        UserDetailContext userDetail= threadLocal.get();
        if(userDetail!= null){
            return Optional.of((T)userDetail);
        }
        Optional<String> detail= HeaderContextHolder.getHeaderValue(UserDetailContext.USER_HEADER_NAME);
        if(detail.isPresent()){
            userDetail= JsonUtil.fromString2Object(detail.get(),userDetailClass);
            threadLocal.set(userDetail);
            return Optional.of((T)userDetail);
        }
        return Optional.empty();
    }

    public static void setUserDetailContext(UserDetailContext ctx){
        threadLocal.set(ctx);
    }

    public static void removeUserDetailContext(){
        threadLocal.remove();
    }
}
