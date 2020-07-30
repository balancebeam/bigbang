package io.anyway.bigbang.framework.core.client;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;

@Slf4j
public class ClientAgentGenericFilterBean extends GenericFilterBean{

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        String remoteAddr = request.getRemoteAddr();
        if(log.isInfoEnabled()) {
            log.info("request path:{}",httpRequest.getRequestURI());
            StringBuilder builder = new StringBuilder();
            builder.append("{");
            Enumeration<String> headerNames = httpRequest.getHeaderNames();
            for (; headerNames.hasMoreElements(); ) {
                String headerName = headerNames.nextElement();
                String headerValue = httpRequest.getHeader(headerName);
                builder.append(headerName);
                builder.append("=");
                builder.append(headerValue);
                builder.append(",");
            }
            builder.append("}");
            log.info("HttpServletRequest headers:{}, remote address: {}", builder.toString(),remoteAddr);
        }
        String text= httpRequest.getHeader("X-Client-Agent");
        if(!StringUtils.isEmpty(text)){
            ClientAgent clientAgent= JSONObject.parseObject(text,ClientAgent.class);
            ClientAgentContextHolder.setClientAgentContext(clientAgent);
        }
        try {
            chain.doFilter(request, response);
        }finally {
            ClientAgentContextHolder.remove();
        }
    }
}
