package io.anyway.bigbang.framework.header;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
@Slf4j
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
        if(headers!= null && headers.containsKey(name)){
            String value= headers.get(name);
            try {
                value= URLDecoder.decode(value,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error("decode value: {} error",value,e);
            }
            return Optional.of(value);
        }
        return Optional.empty();
    }

    public static void remove(){
        threadLocal.remove();
    }
}
