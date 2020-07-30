package io.anyway.bigbang.framework.core.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HeaderDeliveryService {

    @Autowired(required = false)
    private List<HeaderDeliveryInterceptor> interceptors= Collections.emptyList();

    public Map<String,String> headers(){
        Map<String,String> headers= new HashMap<>();
        for(HeaderDeliveryInterceptor each: interceptors){
            each.makeup(headers);
        }
        log.info("service delivery headers: {}",headers);
        return headers;
    }
}
