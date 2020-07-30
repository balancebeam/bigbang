package io.anyway.bigbang.framework.kernel.header;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Configuration
public class headerConfig {

    @Autowired(required = false)
    private List<PrincipleHeaderKey> headerKeyList= Collections.emptyList();

    @Bean
    public GenericFilterBean createHeaderCollectionFilterBean() {
        return new GenericFilterBean(){
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                try{
                    Map<String,String> headers= new HashMap<>();
                    headerKeyList.stream().forEach(each-> {
                        String value= ((HttpServletRequest)request).getHeader(each.name());
                        if(!StringUtils.isEmpty(value)) {
                            headers.put(each.name(), value);
                        }
                    });
                    log.debug("headers: {}",headers);
                    chain.doFilter(request,response);
                }finally {
                    PrincipleHeaderContext.threadLocal.remove();
                    headerKeyList.stream().forEach(each-> each.removeThreadLocal());
                }
            }
        };
    }

    @Bean
    public RequestInterceptor createDeliveryHeaderFeignInterceptor(){
        return template -> {
            Map<String,String> headers= PrincipleHeaderContext.threadLocal.get();
            if(headers!= null){
                for(Map.Entry<String,String> each: headers.entrySet()){
                    template.header(each.getKey(), each.getValue());
                }
            }
        };
    }
}
