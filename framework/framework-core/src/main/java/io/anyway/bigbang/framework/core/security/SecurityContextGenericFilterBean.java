package io.anyway.bigbang.framework.core.security;

import com.alibaba.fastjson.JSONObject;
import io.anyway.bigbang.framework.core.client.ClientAgentContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
public class SecurityContextGenericFilterBean extends GenericFilterBean{

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        String value= httpRequest.getHeader(UserDetail.USER_HEADER_NAME);
        if(StringUtils.isEmpty(value)){
            value= httpRequest.getParameter(UserDetail.USER_HEADER_NAME);
        }
        try {
            if(!StringUtils.isEmpty(value)){
                UserDetail userDetail= JSONObject.parseObject(value,UserDetail.class);
                SecurityContextHolder.setUserDetail(userDetail);
            }
            chain.doFilter(request, response);
        }finally {
            ClientAgentContextHolder.remove();
        }
    }
}
