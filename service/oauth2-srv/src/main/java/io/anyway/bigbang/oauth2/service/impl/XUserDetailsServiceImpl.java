package io.anyway.bigbang.oauth2.service.impl;

import io.anyway.bigbang.oauth2.domain.XUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

public class XUserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        XUserDetails  details= new XUserDetails();
        details.setUid(123);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        details.setTenantId(request.getParameter("tenant_id"));
        details.setUserType(request.getParameter("user_type"));
        details.setUsername("yangzz");
        details.setPassword(passwordEncoder.encode("yangzz"));
        return details;
    }
}
