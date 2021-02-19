package io.anyway.bigbang.framework.gray;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.ttl.TransmittableThreadLocal;
import io.anyway.bigbang.framework.bootstrap.HeaderContextHolder;

import java.util.Optional;


public interface GrayContextHolder {

    ThreadLocal<GrayContext> threadLocal= new TransmittableThreadLocal<>();

    static Optional<GrayContext> getGrayContext(){
        GrayContext grayContext= threadLocal.get();
        if(grayContext!= null){
            return Optional.of(grayContext);
        }
        Optional<String> text= HeaderContextHolder.getHeaderValue(GrayContext.GRAY_NAME);
        if(text.isPresent()){
            grayContext= JSONObject.parseObject(text.get(), GrayContext.class);
            threadLocal.set(grayContext);
            return Optional.of(grayContext);
        }
        return Optional.empty();
    }

    static void setGrayContext(GrayContext ctx){
        threadLocal.set(ctx);
    }

    static void removeGrayContext(){
        threadLocal.remove();
    }
}
