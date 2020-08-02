package io.anyway.bigbang.framework.header;

import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.*;

public abstract class HeaderContextHolder {

    final static ThreadLocal<Map<String,String>> threadLocal = new TransmittableThreadLocal<>();

    public static void setHeaderMapping(Map<String,String> headers){
        threadLocal.set(headers);
    }

    public static Collection<String> getHeaderNames(){
        Map<String,String> map= threadLocal.get();
        return Objects.nonNull(map)? map.keySet(): Collections.emptyList();
    }

    public static Optional<String> getHeaderValue(String name){
        Map<String,String> headers= threadLocal.get();
        if(headers!= null){
            return Optional.of(headers.get(name));
        }
        return Optional.empty();
    }

    public static void remove(){
        threadLocal.remove();
    }
}
