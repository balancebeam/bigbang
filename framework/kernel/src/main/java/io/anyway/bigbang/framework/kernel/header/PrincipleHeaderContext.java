package io.anyway.bigbang.framework.kernel.header;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Map;
import java.util.Optional;

public abstract class PrincipleHeaderContext {

    final static ThreadLocal<Map<String,String>> threadLocal = new TransmittableThreadLocal<>();

    public static Optional<String> getHeader(String name){
        Map<String,String> headers= threadLocal.get();
        if(headers!= null){
            return Optional.of(headers.get(name));
        }
        return Optional.empty();
    }
}
