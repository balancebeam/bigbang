package io.anyway.bigbang.oauth2.service;


import io.anyway.bigbang.oauth2.domain.XUserDetails;
import io.anyway.bigbang.oauth2.mapper.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


@Slf4j
public class XUserDetailsServiceImpl implements UserDetailsService {

    private HttpServletRequest servletRequest;

    private UserRepository userRepository;

    @Resource
    public void setServletRequest(HttpServletRequest servletRequest) {
        this.servletRequest = servletRequest;
    }

    @Resource
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        XUserDetails details = new XUserDetails();
        //TODO query user message from database or other storage
        return details;
    }
}
