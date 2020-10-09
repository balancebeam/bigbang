package io.anyway.bigbang.framework.logging.marker;


import com.alibaba.ttl.TransmittableThreadLocal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoggingMarkerContext {

    private static ThreadLocal<List<String>> threadLocal= new TransmittableThreadLocal<>();

    public static List<String> markers(){
        return threadLocal.get()!= null? threadLocal.get(): Collections.emptyList();
    }

    static void markers(String... markers){
        if(markers!=null && markers.length> 0) {
            threadLocal.set(Arrays.asList(markers));
        }
    }

    static void remove(){
        threadLocal.remove();
    }
}
