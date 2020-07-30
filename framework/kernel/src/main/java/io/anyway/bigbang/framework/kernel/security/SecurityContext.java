package io.anyway.bigbang.framework.kernel.security;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.ttl.TransmittableThreadLocal;
import io.anyway.bigbang.framework.kernel.header.PrincipleHeaderContext;

import java.util.Optional;

public interface SecurityContext {

    ThreadLocal<UserDetail> threadLocal= new TransmittableThreadLocal<>();

    static UserDetail getUserDetail(){
        UserDetail userDetail= threadLocal.get();
        if(userDetail!= null){
            return userDetail;
        }
        Optional<String> detail= PrincipleHeaderContext.getHeader(UserDetail.USER_HEADER_NAME);
        if(detail.isPresent()){
            userDetail= JSONObject.parseObject(detail.get(),UserDetail.class);
            threadLocal.set(userDetail);
            return userDetail;
        }
        return null;
    }
}
