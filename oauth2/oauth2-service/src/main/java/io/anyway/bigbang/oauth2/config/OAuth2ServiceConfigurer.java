package io.anyway.bigbang.oauth2.config;

import io.anyway.bigbang.framework.header.HeaderContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.annotation.Resource;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Configuration
public class OAuth2ServiceConfigurer {

    @Resource
    private AuthorizationServerEndpointsConfiguration cfg;

    @Bean
    public ClientDetailsService createClientDetailsService() {
        return cfg.getEndpointsConfigurer().getClientDetailsService();
    }

    @Bean
    public AuthorizationCodeServices createAuthorizationCodeServices() {
        return cfg.getEndpointsConfigurer().getAuthorizationCodeServices();
    }

    @Bean
    public OAuth2RequestFactory createOAuth2RequestFactory() {
        return cfg.getEndpointsConfigurer().getOAuth2RequestFactory();
    }

}
