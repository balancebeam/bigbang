package io.anyway.bigbang.framework.useragent;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.ttl.TransmittableThreadLocal;
import io.anyway.bigbang.framework.bootstrap.HeaderContextHolder;

import java.util.Optional;

import static io.anyway.bigbang.framework.useragent.UserAgentContext.USER_AGENT_NAME;


public interface UserAgentContextHolder {

    ThreadLocal<UserAgentContext> threadLocal= new TransmittableThreadLocal<>();

    static Optional<UserAgentContext> getUserAgentContext(){
        UserAgentContext userAgentContext= threadLocal.get();
        if(userAgentContext!= null){
            return Optional.of(userAgentContext);
        }
        Optional<String> text= HeaderContextHolder.getHeaderValue(USER_AGENT_NAME);
        if(text.isPresent()){
            userAgentContext= JSONObject.parseObject(text.get(), UserAgentContext.class);
            threadLocal.set(userAgentContext);
            return Optional.of(userAgentContext);
        }
        return Optional.empty();
    }

    static void removeUserAgentContext(){
        threadLocal.remove();
    }
}
