package io.anyway.bigbang.framework.security;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.ttl.TransmittableThreadLocal;
import io.anyway.bigbang.framework.bootstrap.HeaderContextHolder;

import java.util.Optional;

public interface SecurityContextHolder {

    ThreadLocal<UserDetailContext> threadLocal= new TransmittableThreadLocal<>();

    static Optional<UserDetailContext> getUserDetailContext(){
        UserDetailContext userDetail= threadLocal.get();
        if(userDetail!= null){
            return Optional.of(userDetail);
        }
        Optional<String> detail= HeaderContextHolder.getHeaderValue(UserDetailContext.USER_HEADER_NAME);
        if(detail.isPresent()){
            userDetail= JSONObject.parseObject(detail.get(), UserDetailContext.class);
            threadLocal.set(userDetail);
            return Optional.of(userDetail);
        }
        return Optional.empty();
    }

    static void setUserDetailContext(UserDetailContext ctx){
        threadLocal.set(ctx);
    }

    static void removeUserDetailContext(){
        threadLocal.remove();
    }
}
