package io.anyway.bigbang.framework.header.config;


import io.anyway.bigbang.framework.header.HeaderContext;
import io.anyway.bigbang.framework.header.HeaderContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@ConditionalOnClass(Filter.class)
public class HeaderConfigure {

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
