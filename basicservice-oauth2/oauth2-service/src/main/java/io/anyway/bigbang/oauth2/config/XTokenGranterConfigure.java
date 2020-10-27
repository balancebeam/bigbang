package io.anyway.bigbang.oauth2.config;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.*;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenGranter;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeTokenGranter;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.implicit.ImplicitTokenGranter;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;

import javax.annotation.Resource;
import java.util.*;

@Configuration
@AutoConfigureAfter(OAuth2ServiceConfigurer.class)
public class XTokenGranterConfigure implements SmartInitializingSingleton {

    @Resource
    private AuthorizationEndpoint authorizationEndpoint;

    @Resource
    private TokenEndpoint tokenEndpoint;

    @Autowired(required = false)
    private AuthorizationServerEndpointsConfiguration cfg;

    @Autowired(required = false)
    private List<TokenGranter> extensionTokenGranters = Collections.emptyList();

    @Autowired(required = false)
    private AuthenticationManager authenticationManager;

    @Override
    public void afterSingletonsInstantiated() {
        AuthorizationServerEndpointsConfigurer endpoints = cfg.getEndpointsConfigurer();
        TokenGranter tokenGranter = new TokenGranter() {
            private CompositeTokenGranter delegate;

            @Override
            public OAuth2AccessToken grant(String grantType, TokenRequest tokenRequest) {
                if (delegate == null) {
                    ClientDetailsService clientDetails = endpoints.getClientDetailsService();
                    AuthorizationServerTokenServices tokenServices = endpoints.getTokenServices();
                    AuthorizationCodeServices authorizationCodeServices = endpoints.getAuthorizationCodeServices();
                    OAuth2RequestFactory requestFactory = endpoints.getOAuth2RequestFactory();

                    List<TokenGranter> tokenGranters = new ArrayList<>();
                    tokenGranters.add(new AuthorizationCodeTokenGranter(tokenServices, authorizationCodeServices, clientDetails,
                            requestFactory));
                    tokenGranters.add(new RefreshTokenGranter(tokenServices, clientDetails, requestFactory));
                    ImplicitTokenGranter implicit = new ImplicitTokenGranter(tokenServices, clientDetails, requestFactory);
                    tokenGranters.add(implicit);
                    tokenGranters.add(new ClientCredentialsTokenGranter(tokenServices, clientDetails, requestFactory));

                    if (authenticationManager != null) {
                        tokenGranters.add(new ResourceOwnerPasswordTokenGranter(authenticationManager, tokenServices,
                                clientDetails, requestFactory));
                    }
                    delegate = new CompositeTokenGranter(tokenGranters);
                    for (TokenGranter each : extensionTokenGranters) {
                        delegate.addTokenGranter(each);
                    }
                }
                return delegate.grant(grantType, tokenRequest);
            }
        };
        tokenEndpoint.setTokenGranter(tokenGranter);
        authorizationEndpoint.setTokenGranter(tokenGranter);

        Set<HttpMethod> allowedMethods = new HashSet<>(Arrays.asList(HttpMethod.GET, HttpMethod.POST));
        tokenEndpoint.setAllowedRequestMethods(allowedMethods);
    }
}
