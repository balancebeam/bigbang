package io.anyway.bigbang.framework.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.anyway.bigbang.framework.core.interceptor.HeaderDeliveryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;


@Component
@Slf4j
@ConditionalOnClass(RequestInterceptor.class)
public class FeignSecurityInterceptor implements RequestInterceptor {

    @Resource
    private HeaderDeliveryService headerDeliveryService;

    @Override
    public void apply(RequestTemplate template) {
        Map<String,String> headers= headerDeliveryService.headers();
        for(Map.Entry<String,String> each: headers.entrySet()){
            template.header(each.getKey(),each.getValue());
        }
    }
}
