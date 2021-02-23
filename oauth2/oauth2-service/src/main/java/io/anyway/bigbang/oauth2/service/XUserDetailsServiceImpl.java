package io.anyway.bigbang.oauth2.service;


import io.anyway.bigbang.oauth2.domain.XUserDetails;
import io.anyway.bigbang.oauth2.entity.UserDetailEntity;
import io.anyway.bigbang.oauth2.mapper.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


@Slf4j
public class XUserDetailsServiceImpl implements UserDetailsService {

    private UserRepository userRepository;

    @Resource
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        HttpServletRequest request = getRequest();
        String appId= request.getParameter("client_id");
        UserDetailEntity entity= userRepository.getUserDetail(appId,userId);
        if(entity== null){
            throw new UsernameNotFoundException(appId+":"+userId);
        }
        XUserDetails details = new XUserDetails();
        details.setUsername(userId);
        details.setAppId(entity.getAppId());
        details.setLoginName(entity.getUserName());
        details.setUserType("b");
        details.setPassword(entity.getPassword());
        return details;
    }

    private HttpServletRequest getRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getRequest();
        return request;
    }
}
