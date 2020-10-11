package io.anyway.bigbang.framework.bootstrap;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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
@ConditionalOnClass(GenericFilterBean.class)
public class HeaderConfig {

    @Autowired(required = false)
    private List<HeaderContext> HeaderContextList= Collections.emptyList();

    @Bean
    public GenericFilterBean createHeaderCollectionFilterBean() {

        return new GenericFilterBean(){
            @Override
            public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
                try{
                    Map<String,String> headers= new HashMap<>();
                    HeaderContextList.stream().forEach(each-> {
                        String value= ((HttpServletRequest)request).getHeader(each.getName());
                        if(!StringUtils.isEmpty(value)) {
                            headers.put(each.getName(), value);
                        }
                    });
                    HeaderContextHolder.setHeaderMapping(headers);
                    log.debug("headers: ",headers);
                    chain.doFilter(request,response);
                }finally {
                    HeaderContextHolder.remove();
                    HeaderContextList.stream().forEach(each-> each.removeThreadLocal());
                }
            }
        };
    }
}
