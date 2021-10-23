package io.anyway.bigbang.example.service;

import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Order(Integer.MIN_VALUE)
@WebFilter(filterName = "test",urlPatterns= "/*")
public class TestFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
       System.out.println("123");
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ((HttpServletRequest)request).getHeaderNames();
        chain.doFilter(request,response);
    }
}
