package io.anyway.bigbang.framework.discovery;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.ttl.TransmittableThreadLocal;
import io.anyway.bigbang.framework.header.HeaderContextHolder;

import java.util.Optional;

import static io.anyway.bigbang.framework.discovery.GrayRouteContext.GRAY_CLUSTER_NAME;


public interface GrayRouteContextHolder {

    ThreadLocal<GrayRouteContext> threadLocal= new TransmittableThreadLocal<>();

    static Optional<GrayRouteContext> getGrayRouteContext(){
        GrayRouteContext grayRouteContext= threadLocal.get();
        if(grayRouteContext!= null){
            return Optional.of(grayRouteContext);
        }
        Optional<String> text= HeaderContextHolder.getHeaderValue(GRAY_CLUSTER_NAME);
        if(text.isPresent()){
            grayRouteContext= JSONObject.parseObject(text.get(), GrayRouteContext.class);
            threadLocal.set(grayRouteContext);
            return Optional.of(grayRouteContext);
        }
        return Optional.empty();
    }

    static void removeGrayRouteContext(){
        threadLocal.remove();
    }
}
