package io.anyway.bigbang.framework.kernel.useragent;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.ttl.TransmittableThreadLocal;
import io.anyway.bigbang.framework.kernel.header.PrincipleHeaderContext;

import java.util.Optional;

import static io.anyway.bigbang.framework.kernel.useragent.UserAgent.USER_AGENT_NAME;

public interface UserAgentContext {

    ThreadLocal<UserAgent> threadLocal= new TransmittableThreadLocal<>();

    static UserAgent getUserAgent(){
        UserAgent userAgent= threadLocal.get();
        if(userAgent!= null){
            return userAgent;
        }
        Optional<String> text= PrincipleHeaderContext.getHeader(USER_AGENT_NAME);
        if(text.isPresent()){
            userAgent= JSONObject.parseObject(text.get(),UserAgent.class);
            threadLocal.set(userAgent);
            return userAgent;
        }
        return null;
    }
}
