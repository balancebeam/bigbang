package io.anyway.bigbang.oauth2.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;


@Configuration
public class OAuth2ServiceConfigurer {

    @Autowired(required = false)
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
